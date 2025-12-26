package com.wsd.blogapi.auth.dto;

import com.wsd.blogapi.user.User;
import com.wsd.blogapi.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "사용자 정보 응답")
public class UserResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "권한", example = "ROLE_USER")
    private UserRole role;

    @Schema(description = "계정 상태", example = "ACTIVE")
    private String status;

    @Schema(description = "인증 제공자", example = "LOCAL")
    private String provider;

    @Schema(description = "생성일시", example = "2025-12-26T10:00:00")
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
