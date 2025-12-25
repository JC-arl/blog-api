package com.wsd.blogapi.post;

import com.wsd.blogapi.category.Category;
import com.wsd.blogapi.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 상태별 페이징 조회
    Page<Post> findByStatus(String status, Pageable pageable);

    // 제목 또는 내용으로 검색 (PUBLISHED만)
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 작성자별 게시글 조회
    Page<Post> findByAuthor(User author, Pageable pageable);

    // 작성자별 + 상태별 조회
    Page<Post> findByAuthorAndStatus(User author, String status, Pageable pageable);

    // ID로 조회 (상태 체크)
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.status = :status")
    Optional<Post> findByIdAndStatus(@Param("id") Long id, @Param("status") String status);

    // PUBLISHED 게시글만 조회
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.status = 'PUBLISHED'")
    Optional<Post> findPublishedById(@Param("id") Long id);

    // 카테고리별 게시글 조회
    Page<Post> findByCategoryAndStatus(Category category, String status, Pageable pageable);

    // 상태별 카운트
    Long countByStatus(String status);
}
