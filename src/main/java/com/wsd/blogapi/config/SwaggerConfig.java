package com.wsd.blogapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.backend-url}")
    private String backendUrl;

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";
        String firebase = "Firebase";

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(jwt)
                .addList(firebase);

        Components components = new Components()
                .addSecuritySchemes(jwt, new SecurityScheme()
                        .name(jwt)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT 토큰을 입력하세요 (Bearer 제외)")
                )
                .addSecuritySchemes(firebase, new SecurityScheme()
                        .name(firebase)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("Firebase ID Token")
                        .description("Firebase ID Token을 입력하세요")
                );

        return new OpenAPI()
                .components(components)
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .servers(List.of(
                        new Server().url(backendUrl).description("API Server"),
                        new Server().url("http://localhost:8080").description("Development Server")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("Blog API")
                .description("""
                        ## 동아리 블로그 백엔드 API 문서

                        ### 인증 방법
                        1. **JWT 인증**: /auth/login으로 로그인 후 받은 Access Token을 사용
                        2. **Firebase 인증**: Firebase Authentication으로 발급받은 ID Token 사용

                        ### 주요 기능
                        - 사용자 관리 (회원가입, 로그인, 프로필)
                        - 게시글 CRUD (작성, 조회, 수정, 삭제)
                        - 댓글 관리
                        - 좋아요 기능
                        - 카테고리 관리
                        - 관리자 기능 (사용자 관리, 통계)

                        ### 테스트 계정
                        - 관리자: admin@blog.com / admin1234
                        - 일반 사용자: user1@blog.com / user1234
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Blog API Team")
                        .email("support@blogapi.com")
                )
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                );
    }
}
