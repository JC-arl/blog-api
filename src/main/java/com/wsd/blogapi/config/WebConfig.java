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
        // 정적 리소스 핸들러 (static 폴더의 파일들)
        registry.addResourceHandler("/static/**", "/favicon.ico", "/logo*.png", "/manifest.json", "/robots.txt", "/asset-manifest.json")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);

        // React SPA를 위한 fallback 설정 (루트 제외)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // API 경로는 Spring MVC가 처리하도록 null 반환
                        if (resourcePath.startsWith("api/") ||
                            resourcePath.startsWith("auth/") ||
                            resourcePath.startsWith("oauth2/") ||
                            resourcePath.startsWith("health") ||
                            resourcePath.startsWith("swagger-ui") ||
                            resourcePath.startsWith("v3/api-docs") ||
                            resourcePath.startsWith("posts") ||
                            resourcePath.startsWith("comments") ||
                            resourcePath.startsWith("categories") ||
                            resourcePath.startsWith("admin")) {
                            return null;
                        }

                        Resource requestedResource = location.createRelative(resourcePath);

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
