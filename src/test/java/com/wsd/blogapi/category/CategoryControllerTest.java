package com.wsd.blogapi.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsd.blogapi.category.dto.CreateCategoryRequest;
import com.wsd.blogapi.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    private String adminToken;

    @BeforeEach
    void setUp() {
        // 관리자 토큰 (userId=1, ROLE_ADMIN)
        adminToken = jwtProvider.createAccessToken(1L, "ROLE_ADMIN");
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getAllCategoriesSuccess() throws Exception {
        mockMvc.perform(get("/categories")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("카테고리 생성 성공 (관리자)")
    void createCategorySuccess() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(
                "새 카테고리",
                "new-category",
                "새로운 카테고리 설명"
        );

        mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("새 카테고리"))
                .andExpect(jsonPath("$.slug").value("new-category"))
                .andExpect(jsonPath("$.description").value("새로운 카테고리 설명"));
    }
}
