package com.wsd.blogapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "email은 필수입니다.")
    private String email;

    @NotBlank(message = "password는 필수입니다.")
    private String password;

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
