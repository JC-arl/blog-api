package com.wsd.blogapi.security;

import com.wsd.blogapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .addHeaderWriter((request, response) -> {
                            response.setHeader("Cross-Origin-Opener-Policy", "same-origin-allow-popups");
                            response.setHeader("Cross-Origin-Embedder-Policy", "unsafe-none");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/static/**",
                                "/favicon.ico",
                                "/manifest.json",
                                "/logo*.png",
                                "/robots.txt",
                                "/asset-manifest.json"
                        ).permitAll()
                        // Health & Swagger
                        .requestMatchers("/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Auth - 인증 불필요 엔드포인트
                        .requestMatchers("/auth/signup", "/auth/login", "/auth/refresh", "/auth/kakao-login").permitAll()
                        // OAuth
                        .requestMatchers("/oauth/**").permitAll()
                        // Posts - GET만 허용
                        .requestMatchers(HttpMethod.GET, "/posts", "/posts/**").permitAll()
                        // Categories - GET만 허용
                        .requestMatchers(HttpMethod.GET, "/categories", "/categories/**").permitAll()
                        // Comments - GET만 허용
                        .requestMatchers(HttpMethod.GET, "/comments/**").permitAll()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new FirebaseAuthFilter(userRepository), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}