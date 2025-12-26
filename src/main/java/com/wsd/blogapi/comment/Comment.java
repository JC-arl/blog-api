package com.wsd.blogapi.comment;

import com.wsd.blogapi.post.Post;
import com.wsd.blogapi.user.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, DELETED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Comment() {}

    public Comment(String content, Post post, User author) {
        this.content = content;
        this.post = post;
        this.author = author;
        this.status = "ACTIVE";
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

    // Business methods
    public void updateContent(String content) {
        this.content = content;
    }

    public void delete() {
        this.status = "DELETED";
    }

    public boolean isAuthor(Long userId) {
        return this.author.getId().equals(userId);
    }

    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }
}
