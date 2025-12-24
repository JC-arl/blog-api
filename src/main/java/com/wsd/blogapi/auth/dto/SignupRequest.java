package com.wsd.blogapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "email은 필수입니다.")
    private String email;

    @NotBlank(message = "password는 필수입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8~100자여야 합니다.")
    private String password;

    @NotBlank(message = "nickname은 필수입니다.")
    @Size(min = 2, max = 50, message = "닉네임은 2~50자여야 합니다.")
    private String nickname;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }
}
