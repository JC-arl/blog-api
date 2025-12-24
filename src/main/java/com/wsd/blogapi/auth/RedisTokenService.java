package com.wsd.blogapi.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class RedisTokenService {

    private final StringRedisTemplate redisTemplate;
    private final long refreshExpSeconds;

    public RedisTokenService(StringRedisTemplate redisTemplate,
                             @Value("${app.jwt.refresh-exp-seconds}") long refreshExpSeconds) {
        this.redisTemplate = redisTemplate;
        this.refreshExpSeconds = refreshExpSeconds;
    }

    private String key(Long userId) {
        return "refresh:" + userId;
    }

    public void saveRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(key(userId), refreshToken, Duration.ofSeconds(refreshExpSeconds));
    }

    public Optional<String> getRefreshToken(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(userId)));
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(key(userId));
    }
}
