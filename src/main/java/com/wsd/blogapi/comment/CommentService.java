package com.wsd.blogapi.comment;

import com.wsd.blogapi.comment.dto.CommentResponse;
import com.wsd.blogapi.comment.dto.CreateCommentRequest;
import com.wsd.blogapi.comment.dto.UpdateCommentRequest;
import com.wsd.blogapi.post.Post;
import com.wsd.blogapi.post.PostRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 댓글 생성
     */
    @Transactional
    public CommentResponse createComment(Long postId, CreateCommentRequest request, Long authorId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        // 게시글이 PUBLISHED 상태인지 확인
        if (!post.isPublished()) {
            throw new IllegalStateException("공개된 게시글에만 댓글을 작성할 수 있습니다");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Comment comment = new Comment(request.content(), post, author);
        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }

    /**
     * 게시글별 댓글 목록 조회 (ACTIVE만)
     */
    public Page<CommentResponse> getCommentsByPost(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        return commentRepository.findActiveCommentsByPost(post, pageable)
                .map(CommentResponse::from);
    }

    /**
     * 댓글 단건 조회
     */
    public CommentResponse getComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));

        if (!comment.isActive()) {
            throw new IllegalArgumentException("삭제된 댓글입니다");
        }

        return CommentResponse.from(comment);
    }

    /**
     * 내 댓글 목록 조회
     */
    public Page<CommentResponse> getMyComments(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        return commentRepository.findByAuthor(user, pageable)
                .map(CommentResponse::from);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentResponse updateComment(Long id, UpdateCommentRequest request, Long userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));

        if (!comment.isAuthor(userId)) {
            throw new IllegalStateException("댓글 작성자만 수정할 수 있습니다");
        }

        if (!comment.isActive()) {
            throw new IllegalStateException("삭제된 댓글은 수정할 수 없습니다");
        }

        comment.updateContent(request.content());
        return CommentResponse.from(comment);
    }

    /**
     * 댓글 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));

        if (!comment.isAuthor(userId)) {
            throw new IllegalStateException("댓글 작성자만 삭제할 수 있습니다");
        }

        comment.delete();
    }

    /**
     * 댓글 강제 삭제 (관리자 전용)
     */
    @Transactional
    public void forceDeleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));

        commentRepository.delete(comment);
    }

    /**
     * 게시글별 댓글 수 조회
     */
    public Long getCommentCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        return commentRepository.countActiveCommentsByPost(post);
    }
}
