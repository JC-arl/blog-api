package com.wsd.blogapi.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsd.blogapi.auth.dto.LoginRequest;
import com.wsd.blogapi.auth.dto.SignupRequest;
import com.wsd.blogapi.auth.dto.RefreshRequest;
import com.wsd.blogapi.auth.dto.TokenResponse;
import com.wsd.blogapi.security.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공")
    void signupSuccess() throws Exception {
        SignupRequest request = new SignupRequest(
                "newuser@test.com",
                "password1234",
                "새로운사용자"
        );

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("로그인 성공")
    void loginSuccess() throws Exception {
        // 먼저 회원가입
        SignupRequest signupRequest = new SignupRequest(
                "logintest@test.com",
                "password1234",
                "로그인테스트"
        );

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        // 로그인
        LoginRequest request = new LoginRequest(
                "logintest@test.com",
                "password1234"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void loginFailWithWrongPassword() throws Exception {
        // 먼저 회원가입
        SignupRequest signupRequest = new SignupRequest(
                "wrongpw@test.com",
                "password1234",
                "비밀번호틀림테스트"
        );

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        // 잘못된 비밀번호로 로그인
        LoginRequest request = new LoginRequest(
                "wrongpw@test.com",
                "wrongpassword"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signupFailWithDuplicateEmail() throws Exception {
        // 먼저 회원가입
        SignupRequest firstRequest = new SignupRequest(
                "duplicate@test.com",
                "password1234",
                "첫번째사용자"
        );

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // 같은 이메일로 다시 회원가입 시도
        SignupRequest duplicateRequest = new SignupRequest(
                "duplicate@test.com",
                "password1234",
                "중복테스트"
        );

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 실패 - 유효성 검증 실패 (짧은 비밀번호)")
    void signupFailWithShortPassword() throws Exception {
        SignupRequest request = new SignupRequest(
                "test@test.com",
                "short", // 8자 미만
                "테스트"
        );

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
