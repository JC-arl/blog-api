package com.wsd.blogapi.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {

    private final String timestamp;
    private final String path;
    private final int status;
    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Map<String, Object> details;

    public ErrorResponse(String path, int status, String code, String message, Map<String, Object> details) {
        this.timestamp = Instant.now().toString();
        this.path = path;
        this.status = status;
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public String getTimestamp() { return timestamp; }
    public String getPath() { return path; }
    public int getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public Map<String, Object> getDetails() { return details; }

    public static ErrorResponse of(String path, ErrorCode errorCode, String message, Map<String, Object> details) {
        return new ErrorResponse(path, errorCode.getHttpStatus().value(), errorCode.getCode(), message, details);
    }

    public static ErrorResponse of(String path, ErrorCode errorCode) {
        return of(path, errorCode, errorCode.getDefaultMessage(), null);
    }
}
