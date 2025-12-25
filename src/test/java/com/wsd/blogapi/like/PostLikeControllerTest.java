package com.wsd.blogapi.like;

import com.wsd.blogapi.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = jwtProvider.createAccessToken(1L, "ROLE_USER");
    }

    @Test
    @DisplayName("좋아요 토글 성공 - 좋아요 추가")
    void toggleLikeSuccess() throws Exception {
        Long postId = 1L; // 시드 데이터의 게시글 ID

        mockMvc.perform(post("/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").exists())
                .andExpect(jsonPath("$.likeCount").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("좋아요 수 조회 성공")
    void getLikeCountSuccess() throws Exception {
        Long postId = 1L; // 시드 데이터의 게시글 ID

        mockMvc.perform(get("/posts/{postId}/like/count", postId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    @DisplayName("좋아요 상태 확인 성공")
    void getLikeStatusSuccess() throws Exception {
        Long postId = 1L; // 시드 데이터의 게시글 ID

        mockMvc.perform(get("/posts/{postId}/like/status", postId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").exists())
                .andExpect(jsonPath("$.likeCount").exists());
    }
}
