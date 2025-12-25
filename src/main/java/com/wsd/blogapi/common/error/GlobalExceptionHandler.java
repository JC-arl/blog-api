package com.wsd.blogapi.common.error;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            details.put(fe.getField(), fe.getDefaultMessage());
        }

        // 유효성 검증 실패 로그 (WARN 레벨, 민감정보 제외)
        log.warn("[VALIDATION_ERROR] URI: {}, Errors: {}", req.getRequestURI(), details);

        ErrorResponse body = ErrorResponse.of(req.getRequestURI(), ErrorCode.VALIDATION_FAILED, "입력 값이 올바르지 않습니다.", details);
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getHttpStatus()).body(body);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException e, HttpServletRequest req) {
        // JWT 만료는 자주 발생하므로 INFO 레벨
        log.info("[TOKEN_EXPIRED] URI: {}, Message: {}", req.getRequestURI(), e.getMessage());

        return ResponseEntity.status(ErrorCode.TOKEN_EXPIRED.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.TOKEN_EXPIRED));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwt(JwtException e, HttpServletRequest req) {
        // JWT 검증 실패 로그 (WARN 레벨, 토큰 값은 제외)
        log.warn("[INVALID_TOKEN] URI: {}, Message: {}", req.getRequestURI(), e.getMessage());

        return ResponseEntity.status(ErrorCode.INVALID_TOKEN.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.INVALID_TOKEN, e.getMessage(), null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleDenied(AccessDeniedException e, HttpServletRequest req) {
        // 접근 거부 로그 (WARN 레벨)
        log.warn("[ACCESS_DENIED] URI: {}, Message: {}", req.getRequestURI(), e.getMessage());

        return ResponseEntity.status(ErrorCode.FORBIDDEN.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.FORBIDDEN));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDup(DataIntegrityViolationException e, HttpServletRequest req) {
        // 데이터 무결성 위반 로그 (WARN 레벨, SQL 정보는 제외하고 메시지만)
        log.warn("[DATA_INTEGRITY_VIOLATION] URI: {}, Message: {}", req.getRequestURI(), e.getMostSpecificCause().getMessage());

        return ResponseEntity.status(ErrorCode.DUPLICATE_RESOURCE.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.DUPLICATE_RESOURCE));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest req) {
        // 잘못된 인자 로그 (WARN 레벨)
        log.warn("[ILLEGAL_ARGUMENT] URI: {}, Message: {}", req.getRequestURI(), e.getMessage());

        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.BAD_REQUEST, e.getMessage(), null));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e, HttpServletRequest req) {
        // 잘못된 상태 로그 (WARN 레벨)
        log.warn("[ILLEGAL_STATE] URI: {}, Message: {}", req.getRequestURI(), e.getMessage());

        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.BAD_REQUEST, e.getMessage(), null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException e, HttpServletRequest req) {
        // 예상치 못한 런타임 에러 - ERROR 레벨로 스택트레이스 전체 로그
        log.error("[INTERNAL_SERVER_ERROR] URI: {}, Message: {}", req.getRequestURI(), e.getMessage(), e);

        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest req) {
        // 모든 예외의 최종 핸들러 - ERROR 레벨로 스택트레이스 전체 로그
        log.error("[UNEXPECTED_ERROR] URI: {}, Message: {}", req.getRequestURI(), e.getMessage(), e);

        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", null));
    }
}
