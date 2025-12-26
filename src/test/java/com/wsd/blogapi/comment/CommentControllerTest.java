package com.wsd.blogapi.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsd.blogapi.category.Category;
import com.wsd.blogapi.category.CategoryRepository;
import com.wsd.blogapi.comment.dto.CreateCommentRequest;
import com.wsd.blogapi.comment.dto.UpdateCommentRequest;
import com.wsd.blogapi.post.Post;
import com.wsd.blogapi.post.PostRepository;
import com.wsd.blogapi.security.JwtProvider;
import com.wsd.blogapi.security.TestSecurityConfig;
import com.wsd.blogapi.user.User;
import com.wsd.blogapi.user.UserRepository;
import com.wsd.blogapi.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessToken;
    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = new User(
                "test@example.com",
                passwordEncoder.encode("password123"),
                "테스트유저",
                UserRole.ROLE_USER,
                "ACTIVE",
                "LOCAL",
                null
        );
        testUser = userRepository.save(testUser);

        // 테스트용 카테고리 생성
        Category testCategory = new Category("테스트카테고리", "test-category", "테스트용 카테고리입니다");
        testCategory = categoryRepository.save(testCategory);

        // 테스트용 게시글 생성
        testPost = new Post("테스트 게시글", "테스트 내용", testUser);
        testPost.updateCategory(testCategory);
        testPost = postRepository.save(testPost);

        // 테스트용 토큰 생성
        accessToken = jwtProvider.createAccessToken(testUser.getId(), "ROLE_USER");
    }

    @Test
    @DisplayName("댓글 생성 성공")
    void createCommentSuccess() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest(
                "테스트 댓글입니다."
        );

        mockMvc.perform(post("/posts/{postId}/comments", testPost.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("테스트 댓글입니다."))
                .andExpect(jsonPath("$.postId").value(testPost.getId()))
                .andExpect(jsonPath("$.authorId").value(testUser.getId()));
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getCommentsSuccess() throws Exception {
        mockMvc.perform(get("/posts/{postId}/comments", testPost.getId())
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
        // 먼저 댓글 생성
        CreateCommentRequest createRequest = new CreateCommentRequest("원본 댓글");

        String createResponse = mockMvc.perform(post("/posts/{postId}/comments", testPost.getId())
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
        // 먼저 댓글 생성
        CreateCommentRequest createRequest = new CreateCommentRequest("삭제할 댓글");

        String createResponse = mockMvc.perform(post("/posts/{postId}/comments", testPost.getId())
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
