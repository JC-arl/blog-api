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
            String idToken = authHeader.substring(7);

            try {
                // Firebase ID Token 검증
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
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
                // 토큰 검증 실패
                logger.error("Firebase token verification failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
