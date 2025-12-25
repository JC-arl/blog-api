package com.wsd.blogapi.post;

import com.wsd.blogapi.category.Category;
import com.wsd.blogapi.category.CategoryRepository;
import com.wsd.blogapi.post.dto.CreatePostRequest;
import com.wsd.blogapi.post.dto.PostResponse;
import com.wsd.blogapi.post.dto.UpdatePostRequest;
import com.wsd.blogapi.user.User;
import com.wsd.blogapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 게시글 생성
     */
    @Transactional
    public PostResponse createPost(CreatePostRequest request, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Post post = new Post(request.title(), request.content(), author);

        // 카테고리 설정
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));
            post.updateCategory(category);
        }

        if ("DRAFT".equals(request.status())) {
            post.draft();
        }

        Post savedPost = postRepository.save(post);
        return PostResponse.from(savedPost);
    }

    /**
     * 게시글 목록 조회 (PUBLISHED만)
     */
    public Page<PostResponse> getPosts(Pageable pageable) {
        return postRepository.findByStatus("PUBLISHED", pageable)
                .map(PostResponse::from);
    }

    /**
     * 게시글 검색
     */
    public Page<PostResponse> searchPosts(String keyword, Pageable pageable) {
        return postRepository.searchByKeyword(keyword, pageable)
                .map(PostResponse::from);
    }

    /**
     * 게시글 상세 조회 (조회수 증가)
     */
    @Transactional
    public PostResponse getPost(Long id) {
        Post post = postRepository.findPublishedById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        post.incrementViewCount();
        return PostResponse.from(post);
    }

    /**
     * 내 게시글 목록 조회 (모든 상태)
     */
    public Page<PostResponse> getMyPosts(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        return postRepository.findByAuthor(user, pageable)
                .map(PostResponse::from);
    }

    /**
     * 카테고리별 게시글 목록 조회
     */
    public Page<PostResponse> getPostsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));

        return postRepository.findByCategoryAndStatus(category, "PUBLISHED", pageable)
                .map(PostResponse::from);
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public PostResponse updatePost(Long id, UpdatePostRequest request, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        if (!post.isAuthor(userId)) {
            throw new IllegalStateException("게시글 작성자만 수정할 수 있습니다");
        }

        if (request.title() != null) {
            post.updateTitle(request.title());
        }

        if (request.content() != null) {
            post.updateContent(request.content());
        }

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));
            post.updateCategory(category);
        }

        if (request.status() != null) {
            switch (request.status()) {
                case "PUBLISHED" -> post.publish();
                case "DRAFT" -> post.draft();
                case "DELETED" -> post.delete();
            }
        }

        return PostResponse.from(post);
    }

    /**
     * 게시글 삭제 (소프트 삭제)
     */
    @Transactional
    public void deletePost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        if (!post.isAuthor(userId)) {
            throw new IllegalStateException("게시글 작성자만 삭제할 수 있습니다");
        }

        post.delete();
    }

    /**
     * 게시글 강제 삭제 (관리자 전용)
     */
    @Transactional
    public void forceDeletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        postRepository.delete(post);
    }
}
