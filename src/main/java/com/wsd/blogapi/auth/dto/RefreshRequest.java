package com.wsd.blogapi.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshRequest {

    @NotBlank(message = "refreshToken은 필수입니다.")
    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
}
