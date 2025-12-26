package com.wsd.blogapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // React SPA를 위한 fallback 설정
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // 빈 경로나 루트 경로는 index.html로 처리
                        if (resourcePath == null || resourcePath.isEmpty() || ".".equals(resourcePath)) {
                            return new ClassPathResource("/static/index.html");
                        }

                        Resource requestedResource = location.createRelative(resourcePath);

                        // API 경로는 Spring MVC가 처리하도록 null 반환
                        if (resourcePath.startsWith("api/") ||
                            resourcePath.startsWith("auth/") ||
                            resourcePath.startsWith("oauth2/") ||
                            resourcePath.startsWith("health") ||
                            resourcePath.startsWith("swagger-ui") ||
                            resourcePath.startsWith("v3/api-docs")) {
                            return null;
                        }

                        // 리소스가 존재하면 리소스 반환
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }

                        // 그 외의 경우 index.html로 fallback (React Router 지원)
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}
