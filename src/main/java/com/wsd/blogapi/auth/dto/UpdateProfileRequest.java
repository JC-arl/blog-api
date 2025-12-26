package com.wsd.blogapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "프로필 수정 요청")
public class UpdateProfileRequest {
    @Schema(description = "닉네임", example = "새로운닉네임", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 50, message = "닉네임은 2~50자 사이여야 합니다")
    private String nickname;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
