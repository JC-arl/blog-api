package com.wsd.blogapi.post;

import com.wsd.blogapi.category.Category;
import com.wsd.blogapi.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false, length = 20)
    private String status = "PUBLISHED"; // PUBLISHED, DRAFT, DELETED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Post() {}

    public Post(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.viewCount = 0;
        this.status = "PUBLISHED";
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public User getAuthor() { return author; }
    public Category getCategory() { return category; }
    public Integer getViewCount() { return viewCount; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Business methods
    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateCategory(Category category) {
        this.category = category;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void publish() {
        this.status = "PUBLISHED";
    }

    public void draft() {
        this.status = "DRAFT";
    }

    public void delete() {
        this.status = "DELETED";
    }

    public boolean isAuthor(Long userId) {
        return this.author.getId().equals(userId);
    }

    public boolean isPublished() {
        return "PUBLISHED".equals(this.status);
    }
}
