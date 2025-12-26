package com.wsd.blogapi.admin.dto;

import com.wsd.blogapi.user.UserRole;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 2, max = 50, message = "닉네임은 2~50자 이내여야 합니다")
        String nickname,

        UserRole role,

        String status // ACTIVE, SUSPENDED
) {
}
