package com.wsd.blogapi.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsd.blogapi.post.dto.CreatePostRequest;
import com.wsd.blogapi.post.dto.UpdatePostRequest;
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
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    private String accessToken;

    @BeforeEach
    void setUp() {
        // 테스트용 토큰 생성 (userId=1, ROLE_USER)
        accessToken = jwtProvider.createAccessToken(1L, "ROLE_USER");
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPostSuccess() throws Exception {
        CreatePostRequest request = new CreatePostRequest(
                "테스트 게시글 제목",
                "테스트 게시글 내용입니다.",
                1L,
                "PUBLISHED"
        );

        mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("테스트 게시글 제목"))
                .andExpect(jsonPath("$.content").value("테스트 게시글 내용입니다."))
                .andExpect(jsonPath("$.authorId").value(1));
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    void getPostsSuccess() throws Exception {
        mockMvc.perform(get("/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getPostSuccess() throws Exception {
        // 시드 데이터에 있는 게시글 ID 사용
        Long postId = 1L;

        mockMvc.perform(get("/posts/{id}", postId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.viewCount").exists());
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePostSuccess() throws Exception {
        // 먼저 게시글 생성
        CreatePostRequest createRequest = new CreatePostRequest(
                "원본 제목",
                "원본 내용",
                1L,
                "PUBLISHED"
        );

        String createResponse = mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long postId = objectMapper.readTree(createResponse).get("id").asLong();

        // 게시글 수정
        UpdatePostRequest updateRequest = new UpdatePostRequest(
                "수정된 제목",
                "수정된 내용",
                1L,
                "PUBLISHED"
        );

        mockMvc.perform(put("/posts/{id}", postId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.content").value("수정된 내용"));
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePostSuccess() throws Exception {
        // 먼저 게시글 생성
        CreatePostRequest createRequest = new CreatePostRequest(
                "삭제할 게시글",
                "삭제될 내용",
                1L,
                "PUBLISHED"
        );

        String createResponse = mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long postId = objectMapper.readTree(createResponse).get("id").asLong();

        // 게시글 삭제
        mockMvc.perform(delete("/posts/{id}", postId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("게시글 검색 성공")
    void searchPostsSuccess() throws Exception {
        mockMvc.perform(get("/posts/search")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "Spring")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
