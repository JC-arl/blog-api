package com.wsd.blogapi.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsd.blogapi.category.Category;
import com.wsd.blogapi.category.CategoryRepository;
import com.wsd.blogapi.post.dto.CreatePostRequest;
import com.wsd.blogapi.post.dto.UpdatePostRequest;
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
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessToken;
    private User testUser;
    private Category testCategory;

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
        testCategory = new Category("테스트카테고리", "test-category", "테스트용 카테고리입니다");
        testCategory = categoryRepository.save(testCategory);

        // 테스트용 토큰 생성
        accessToken = jwtProvider.createAccessToken(testUser.getId(), "ROLE_USER");
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPostSuccess() throws Exception {
        CreatePostRequest request = new CreatePostRequest(
                "테스트 게시글 제목",
                "테스트 게시글 내용입니다.",
                testCategory.getId(),
                "PUBLISHED"
        );

        mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("테스트 게시글 제목"))
                .andExpect(jsonPath("$.content").value("테스트 게시글 내용입니다."))
                .andExpect(jsonPath("$.authorId").value(testUser.getId()));
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
        // 먼저 게시글 생성
        CreatePostRequest createRequest = new CreatePostRequest(
                "조회할 게시글",
                "조회할 내용",
                testCategory.getId(),
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

        // 게시글 상세 조회
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
                testCategory.getId(),
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
                testCategory.getId(),
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
                testCategory.getId(),
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
