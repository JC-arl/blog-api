package com.wsd.blogapi.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.info.BuildProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Health", description = "헬스체크 API")
@RestController
public class HealthController {

    private final BuildProperties buildProperties;
    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;

    public HealthController(BuildProperties buildProperties, DataSource dataSource, RedisTemplate<String, Object> redisTemplate) {
        this.buildProperties = buildProperties;
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
    }

    @Operation(
            summary = "서버 상태 확인",
            description = "서버, MySQL, Redis 상태를 확인합니다. 인증 없이 호출 가능합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @ApiResponse(responseCode = "200", description = "모든 서비스 정상"),
            @ApiResponse(responseCode = "503", description = "일부 서비스 장애")
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> components = new HashMap<>();

        boolean allHealthy = true;

        // MySQL 상태 확인
        Map<String, String> mysqlStatus = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                mysqlStatus.put("status", "UP");
                mysqlStatus.put("database", conn.getCatalog());
            } else {
                mysqlStatus.put("status", "DOWN");
                mysqlStatus.put("error", "Connection validation failed");
                allHealthy = false;
            }
        } catch (Exception e) {
            mysqlStatus.put("status", "DOWN");
            mysqlStatus.put("error", e.getMessage());
            allHealthy = false;
        }
        components.put("mysql", mysqlStatus);

        // Redis 상태 확인
        Map<String, String> redisStatus = new HashMap<>();
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            if ("PONG".equals(pong)) {
                redisStatus.put("status", "UP");
                redisStatus.put("response", pong);
            } else {
                redisStatus.put("status", "DOWN");
                redisStatus.put("error", "Unexpected response: " + pong);
                allHealthy = false;
            }
        } catch (Exception e) {
            redisStatus.put("status", "DOWN");
            redisStatus.put("error", e.getMessage());
            allHealthy = false;
        }
        components.put("redis", redisStatus);

        // 전체 상태
        response.put("status", allHealthy ? "UP" : "DOWN");
        response.put("components", components);
        response.put("version", buildProperties.getVersion());
        response.put("buildTime", buildProperties.getTime().toString());
        response.put("timestamp", Instant.now().toString());

        // 하나라도 DOWN이면 503 Service Unavailable 반환
        HttpStatus statusCode = allHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(statusCode).body(response);
    }
}
