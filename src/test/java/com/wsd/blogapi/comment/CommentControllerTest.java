package com.wsd.blogapi.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsd.blogapi.comment.dto.CreateCommentRequest;
import com.wsd.blogapi.comment.dto.UpdateCommentRequest;
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
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = jwtProvider.createAccessToken(1L, "ROLE_USER");
    }

    @Test
    @DisplayName("댓글 생성 성공")
    void createCommentSuccess() throws Exception {
        Long postId = 1L; // 시드 데이터의 게시글 ID

        CreateCommentRequest request = new CreateCommentRequest(
                "테스트 댓글입니다."
        );

        mockMvc.perform(post("/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("테스트 댓글입니다."))
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.authorId").value(1));
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getCommentsSuccess() throws Exception {
        Long postId = 1L; // 시드 데이터의 게시글 ID

        mockMvc.perform(get("/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateCommentSuccess() throws Exception {
        Long postId = 1L;

        // 먼저 댓글 생성
        CreateCommentRequest createRequest = new CreateCommentRequest("원본 댓글");

        String createResponse = mockMvc.perform(post("/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long commentId = objectMapper.readTree(createResponse).get("id").asLong();

        // 댓글 수정
        UpdateCommentRequest updateRequest = new UpdateCommentRequest("수정된 댓글");

        mockMvc.perform(put("/comments/{id}", commentId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정된 댓글"));
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteCommentSuccess() throws Exception {
        Long postId = 1L;

        // 먼저 댓글 생성
        CreateCommentRequest createRequest = new CreateCommentRequest("삭제할 댓글");

        String createResponse = mockMvc.perform(post("/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long commentId = objectMapper.readTree(createResponse).get("id").asLong();

        // 댓글 삭제
        mockMvc.perform(delete("/comments/{id}", commentId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }
}
