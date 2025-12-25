package com.wsd.blogapi.admin.dto;

import com.wsd.blogapi.user.User;
import com.wsd.blogapi.user.UserRole;

import java.time.LocalDateTime;

public record UserManagementResponse(
        Long id,
        String email,
        String nickname,
        UserRole role,
        String status,
        String provider,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserManagementResponse from(User user) {
        return new UserManagementResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getStatus(),
                user.getProvider(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
