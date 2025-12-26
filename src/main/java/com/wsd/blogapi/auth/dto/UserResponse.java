package com.wsd.blogapi.auth.dto;

import com.wsd.blogapi.user.User;
import com.wsd.blogapi.user.UserRole;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String email;
    private String nickname;
    private UserRole role;
    private String status;
    private String provider;
    private LocalDateTime createdAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.provider = user.getProvider();
        this.createdAt = user.getCreatedAt();
    }

    // Getters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getNickname() { return nickname; }
    public UserRole getRole() { return role; }
    public String getStatus() { return status; }
    public String getProvider() { return provider; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
