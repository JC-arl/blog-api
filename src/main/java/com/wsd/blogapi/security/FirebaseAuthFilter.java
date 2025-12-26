package com.wsd.blogapi.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.wsd.blogapi.user.User;
import com.wsd.blogapi.user.UserRepository;
import com.wsd.blogapi.user.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class FirebaseAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public FirebaseAuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Firebase 토큰인지 JWT 토큰인지 구분
            // Firebase ID Token은 JWT 형식이지만 issuer가 다름
            if (isFirebaseToken(token)) {
                try {
                    // Firebase ID Token 검증
                    FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                    String uid = decodedToken.getUid();
                    String email = decodedToken.getEmail();
                    String name = decodedToken.getName();

                    // 사용자 조회 또는 생성
                    User user = userRepository.findByEmail(email)
                            .orElseGet(() -> {
                                // 신규 사용자 생성
                                User newUser = new User(
                                        email,
                                        null, // Firebase 사용자는 비밀번호 없음
                                        name != null ? name : "Firebase User",
                                        UserRole.ROLE_USER,
                                        "ACTIVE",
                                        "FIREBASE",
                                        uid
                                );
                                return userRepository.save(newUser);
                            });

                    // Spring Security Context에 인증 정보 설정
                    AuthUser authUser = new AuthUser(user);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } catch (FirebaseAuthException e) {
                    // 토큰 검증 실패 - Firebase 토큰이 유효하지 않음
                    logger.error("Firebase token verification failed: " + e.getMessage());
                }
            }
            // JWT 토큰이면 이 필터는 스킵하고 다음 필터(JwtAuthFilter)에서 처리
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Firebase ID Token인지 확인
     * Firebase 토큰은 길이가 일반적으로 900자 이상이며, 복잡한 구조를 가짐
     * 우리의 JWT는 상대적으로 짧음 (보통 200-500자)
     */
    private boolean isFirebaseToken(String token) {
        // Firebase ID Token은 매우 긴 편 (일반적으로 800자 이상)
        // 우리의 JWT는 상대적으로 짧음
        // 간단한 휴리스틱: 길이로 구분
        if (token.length() > 700) {
            return true;
        }

        // 추가로 payload를 디코딩해서 iss 클레임 확인 가능
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                // Firebase 토큰의 issuer는 https://securetoken.google.com/<project-id> 형식
                return payload.contains("securetoken.google.com");
            }
        } catch (Exception e) {
            // 디코딩 실패 시 Firebase 토큰이 아님
            return false;
        }

        return false;
    }
}
