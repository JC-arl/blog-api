package com.wsd.blogapi.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    // 로그에서 제외할 민감한 헤더
    private static final List<String> SENSITIVE_HEADERS = Arrays.asList(
            "authorization", "password", "x-api-key", "cookie", "set-cookie"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        // Request/Response Wrapper 생성 (본문 재사용을 위해)
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // 요청/응답 로그
            logRequestResponse(wrappedRequest, wrappedResponse, duration);

            // 응답 본문을 실제 응답으로 복사 (필수!)
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequestResponse(ContentCachingRequestWrapper request,
                                      ContentCachingResponseWrapper response,
                                      long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        int status = response.getStatus();

        String path = queryString != null ? uri + "?" + queryString : uri;

        // 정상 요청은 INFO, 에러는 WARN/ERROR
        if (status >= 500) {
            log.error("[HTTP] {} {} - Status: {} - Duration: {}ms", method, path, status, duration);
        } else if (status >= 400) {
            log.warn("[HTTP] {} {} - Status: {} - Duration: {}ms", method, path, status, duration);
        } else {
            log.info("[HTTP] {} {} - Status: {} - Duration: {}ms", method, path, status, duration);
        }

        // 디버그 레벨에서 헤더 정보 로그 (민감정보 제외)
        if (log.isDebugEnabled()) {
            logHeaders(request);
        }
    }

    private void logHeaders(ContentCachingRequestWrapper request) {
        StringBuilder headers = new StringBuilder();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            // 민감한 헤더는 마스킹 처리
            if (SENSITIVE_HEADERS.contains(headerName.toLowerCase())) {
                headers.append(headerName).append(": ").append("***MASKED***").append(", ");
            } else {
                headers.append(headerName).append(": ").append(request.getHeader(headerName)).append(", ");
            }
        });
        if (headers.length() > 0) {
            log.debug("[Headers] {}", headers.substring(0, headers.length() - 2));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 정적 리소스는 로그 제외
        return path.startsWith("/static/") ||
               path.startsWith("/favicon.ico") ||
               path.startsWith("/manifest.json") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg") ||
               path.endsWith(".css") ||
               path.endsWith(".js");
    }
}
