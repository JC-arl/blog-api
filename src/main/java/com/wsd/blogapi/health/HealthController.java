package com.wsd.blogapi.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Health", description = "헬스체크 API")
@RestController
public class HealthController {

    private final BuildProperties buildProperties;

    public HealthController(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Operation(
            summary = "서버 상태 확인",
            description = "서버가 정상적으로 동작하는지 확인합니다. 인증 없이 호출 가능합니다."
    )
    @ApiResponse(responseCode = "200", description = "서버 정상")
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("version", buildProperties.getVersion());
        response.put("buildTime", buildProperties.getTime().toString());
        response.put("timestamp", Instant.now().toString());
        return response;
    }
}
