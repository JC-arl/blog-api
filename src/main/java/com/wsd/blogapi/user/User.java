package com.wsd.blogapi.user;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE / SUSPENDED

    @Column(nullable = false, length = 20)
    private String provider; // LOCAL / GOOGLE / FIREBASE

    private String providerId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected User() {}

    public User(String email, String passwordHash, String nickname, UserRole role, String status, String provider, String providerId) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.role = role;
        this.status = status;
        this.provider = provider;
        this.providerId = providerId;
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

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getNickname() { return nickname; }
    public UserRole getRole() { return role; }
    public String getStatus() { return status; }
    public String getProvider() { return provider; }
    public String getProviderId() { return providerId; }

    public boolean isActive() { return "ACTIVE".equalsIgnoreCase(status); }

    // Business methods
    public void suspend() {
        this.status = "SUSPENDED";
    }

    public void activate() {
        this.status = "ACTIVE";
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
