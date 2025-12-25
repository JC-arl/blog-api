package com.wsd.blogapi.like;

import com.wsd.blogapi.post.Post;
import com.wsd.blogapi.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 특정 게시글에 특정 사용자가 좋아요 했는지 확인 (N+1 방지)
    @Query("SELECT pl FROM PostLike pl " +
           "LEFT JOIN FETCH pl.post " +
           "LEFT JOIN FETCH pl.user " +
           "WHERE pl.post = :post AND pl.user = :user")
    Optional<PostLike> findByPostAndUser(@Param("post") Post post, @Param("user") User user);

    // 좋아요 존재 여부
    boolean existsByPostAndUser(Post post, User user);

    // 게시글별 좋아요 수
    Long countByPost(Post post);

    // 사용자가 좋아요한 게시글 목록 (N+1 방지: Post의 author, category도 함께 fetch)
    @Query("SELECT DISTINCT p FROM PostLike pl " +
           "JOIN pl.post p " +
           "LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.category " +
           "WHERE pl.user = :user " +
           "ORDER BY pl.createdAt DESC")
    Page<Post> findLikedPostsByUser(@Param("user") User user, Pageable pageable);

    // 게시글에 좋아요한 사용자 목록 (N+1 방지)
    @Query("SELECT pl FROM PostLike pl " +
           "LEFT JOIN FETCH pl.post " +
           "LEFT JOIN FETCH pl.user " +
           "WHERE pl.post = :post")
    Page<PostLike> findByPost(@Param("post") Post post, Pageable pageable);

    // 사용자가 좋아요한 게시글 ID 목록 (체크용)
    @Query("SELECT pl.post.id FROM PostLike pl WHERE pl.user = :user")
    Page<Long> findLikedPostIdsByUser(@Param("user") User user, Pageable pageable);
}
