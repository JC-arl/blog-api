package com.wsd.blogapi.common.error;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            details.put(fe.getField(), fe.getDefaultMessage());
        }
        ErrorResponse body = ErrorResponse.of(req.getRequestURI(), ErrorCode.VALIDATION_FAILED, "입력 값이 올바르지 않습니다.", details);
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getHttpStatus()).body(body);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException e, HttpServletRequest req) {
        return ResponseEntity.status(ErrorCode.TOKEN_EXPIRED.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.TOKEN_EXPIRED));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwt(JwtException e, HttpServletRequest req) {
        return ResponseEntity.status(ErrorCode.INVALID_TOKEN.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.INVALID_TOKEN, e.getMessage(), null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleDenied(AccessDeniedException e, HttpServletRequest req) {
        return ResponseEntity.status(ErrorCode.FORBIDDEN.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.FORBIDDEN));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDup(DataIntegrityViolationException e, HttpServletRequest req) {
        return ResponseEntity.status(ErrorCode.DUPLICATE_RESOURCE.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.DUPLICATE_RESOURCE));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException e, HttpServletRequest req) {
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), null));
    }
}
