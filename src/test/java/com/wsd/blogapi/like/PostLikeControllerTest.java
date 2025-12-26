package com.wsd.blogapi.like;

import com.wsd.blogapi.category.Category;
import com.wsd.blogapi.category.CategoryRepository;
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
class PostLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("좋아요 토글 성공 - 좋아요 추가")
    void toggleLikeSuccess() throws Exception {
        mockMvc.perform(post("/posts/{postId}/like", testPost.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").exists())
                .andExpect(jsonPath("$.likeCount").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("좋아요 수 조회 성공")
    void getLikeCountSuccess() throws Exception {
        mockMvc.perform(get("/posts/{postId}/like/count", testPost.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    @DisplayName("좋아요 상태 확인 성공")
    void getLikeStatusSuccess() throws Exception {
        mockMvc.perform(get("/posts/{postId}/like/status", testPost.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").exists())
                .andExpect(jsonPath("$.likeCount").exists());
    }
}
