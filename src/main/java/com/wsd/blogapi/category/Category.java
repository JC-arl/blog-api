package com.wsd.blogapi.category;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@Getter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(length = 200)
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Category() {}

    public Category(String name, String slug, String description) {
        this.name = name;
        this.slug = slug;
        this.description = description;
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
    public void updateName(String name) {
        this.name = name;
    }

    public void updateSlug(String slug) {
        this.slug = slug;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}
