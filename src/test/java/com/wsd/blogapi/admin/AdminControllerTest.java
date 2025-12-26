package com.wsd.blogapi.admin;

import com.wsd.blogapi.security.JwtProvider;
import com.wsd.blogapi.security.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    private String adminToken;

    @BeforeEach
    void setUp() {
        // 관리자 토큰 생성 (userId=1, ROLE_ADMIN)
        adminToken = jwtProvider.createAccessToken(1L, "ROLE_ADMIN");
    }

    @Test
    @DisplayName("관리자 - 사용자 목록 조회 성공")
    void getAllUsersSuccess() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    @DisplayName("관리자 - 통계 조회 성공")
    void getStatisticsSuccess() throws Exception {
        mockMvc.perform(get("/admin/statistics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").exists())
                .andExpect(jsonPath("$.totalPosts").exists())
                .andExpect(jsonPath("$.totalComments").exists())
                .andExpect(jsonPath("$.totalLikes").exists())
                .andExpect(jsonPath("$.totalCategories").exists());
    }
}
