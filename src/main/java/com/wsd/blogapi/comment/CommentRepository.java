package com.wsd.blogapi.comment;

import com.wsd.blogapi.post.Post;
import com.wsd.blogapi.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글별 댓글 조회 (ACTIVE만, N+1 방지)
    @Query("SELECT c FROM Comment c " +
           "LEFT JOIN FETCH c.post " +
           "LEFT JOIN FETCH c.author " +
           "WHERE c.post = :post AND c.status = 'ACTIVE' " +
           "ORDER BY c.createdAt ASC")
    Page<Comment> findActiveCommentsByPost(@Param("post") Post post, Pageable pageable);

    // 게시글별 댓글 수 조회 (ACTIVE만)
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post = :post AND c.status = 'ACTIVE'")
    Long countActiveCommentsByPost(@Param("post") Post post);

    // 사용자별 댓글 조회 (N+1 방지)
    @Query("SELECT c FROM Comment c " +
           "LEFT JOIN FETCH c.post " +
           "LEFT JOIN FETCH c.author a " +
           "WHERE a = :author")
    Page<Comment> findByAuthor(@Param("author") User author, Pageable pageable);

    // 게시글별 댓글 조회 (모든 상태, N+1 방지)
    @Query("SELECT c FROM Comment c " +
           "LEFT JOIN FETCH c.post p " +
           "LEFT JOIN FETCH c.author " +
           "WHERE p = :post")
    Page<Comment> findByPost(@Param("post") Post post, Pageable pageable);

    // 상태별 카운트
    Long countByStatus(String status);
}
