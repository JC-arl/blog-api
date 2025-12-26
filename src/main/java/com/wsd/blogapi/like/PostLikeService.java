package com.wsd.blogapi.like;

import com.wsd.blogapi.like.dto.LikeStatusResponse;
import com.wsd.blogapi.like.dto.LikeToggleResponse;
import com.wsd.blogapi.like.dto.PostLikeResponse;
import com.wsd.blogapi.post.Post;
import com.wsd.blogapi.post.PostRepository;
import com.wsd.blogapi.post.dto.PostResponse;
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
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 좋아요 토글 (좋아요/좋아요 취소)
     */
    @Transactional
    public LikeToggleResponse toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        // 게시글이 PUBLISHED 상태인지 확인
        if (!post.isPublished()) {
            throw new IllegalStateException("공개된 게시글에만 좋아요를 누를 수 있습니다");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 이미 좋아요 했는지 확인
        return postLikeRepository.findByPostAndUser(post, user)
                .map(postLike -> {
                    // 이미 좋아요 했으면 삭제
                    postLikeRepository.delete(postLike);
                    Long likeCount = postLikeRepository.countByPost(post);
                    return LikeToggleResponse.unliked(likeCount);
                })
                .orElseGet(() -> {
                    // 좋아요 하지 않았으면 추가
                    PostLike newLike = new PostLike(post, user);
                    postLikeRepository.save(newLike);
                    Long likeCount = postLikeRepository.countByPost(post);
                    return LikeToggleResponse.liked(likeCount);
                });
    }

    /**
     * 좋아요 상태 확인
     */
    public LikeStatusResponse getLikeStatus(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        boolean liked = postLikeRepository.existsByPostAndUser(post, user);
        Long likeCount = postLikeRepository.countByPost(post);

        return new LikeStatusResponse(liked, likeCount);
    }

    /**
     * 게시글 좋아요 수 조회
     */
    public Long getLikeCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        return postLikeRepository.countByPost(post);
    }

    /**
     * 내가 좋아요한 게시글 목록
     */
    public Page<PostResponse> getMyLikedPosts(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        return postLikeRepository.findLikedPostsByUser(user, pageable)
                .map(PostResponse::from);
    }

    /**
     * 게시글에 좋아요한 사용자 목록
     */
    public Page<PostLikeResponse> getLikesByPost(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        return postLikeRepository.findByPost(post, pageable)
                .map(PostLikeResponse::from);
    }
}
