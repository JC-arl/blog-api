package com.wsd.blogapi.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.wsd.blogapi.auth.dto.KakaoUserInfo;
import com.wsd.blogapi.user.User;
import com.wsd.blogapi.user.UserRepository;
import com.wsd.blogapi.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class KakaoAuthService {

    private static final Logger logger = LoggerFactory.getLogger(KakaoAuthService.class);
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${kakao.rest-api-key}")
    private String kakaoRestApiKey;

    public KakaoAuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 카카오 Access Token으로 사용자 정보 조회
     */
    public KakaoUserInfo getUserInfo(String kakaoAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + kakaoAccessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                request,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("카카오 사용자 정보 조회 실패");
        }

        Long kakaoId = Long.valueOf(body.get("id").toString());
        Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String email = (String) kakaoAccount.get("email");
        String nickname = (String) profile.get("nickname");

        return new KakaoUserInfo(kakaoId, email, nickname);
    }

    /**
     * 카카오 로그인 처리 및 Firebase Custom Token 생성
     */
    @Transactional
    public String processKakaoLogin(String kakaoAccessToken) {
        // 1. 카카오 사용자 정보 조회
        KakaoUserInfo kakaoUserInfo = getUserInfo(kakaoAccessToken);

        // 2. DB에서 사용자 조회 또는 생성 (카카오 ID 기반)
        String kakaoIdStr = kakaoUserInfo.getKakaoId().toString();
        User user = userRepository.findByProviderAndProviderId("KAKAO", kakaoIdStr)
                .orElseGet(() -> {
                    // 신규 사용자 생성
                    // 이메일이 없으면 더미 이메일 생성 (비즈앱이 아닐 경우 이메일 수집 불가)
                    String email = kakaoUserInfo.getEmail() != null
                            ? kakaoUserInfo.getEmail()
                            : "kakao_" + kakaoIdStr + "@kakao.local";

                    User newUser = new User(
                            email,
                            null, // 카카오 로그인은 비밀번호 없음
                            kakaoUserInfo.getNickname(),
                            UserRole.ROLE_USER,
                            "ACTIVE",
                            "KAKAO",
                            kakaoIdStr
                    );
                    logger.info("신규 카카오 사용자 생성 - kakaoId: {}, nickname: {}", kakaoIdStr, kakaoUserInfo.getNickname());
                    return userRepository.save(newUser);
                });

        // 3. Firebase Custom Token 생성
        try {
            // Firebase에서는 UID를 문자열로 사용
            String firebaseUid = "kakao_" + user.getId();

            // 추가 클레임 (선택사항)
            Map<String, Object> claims = new HashMap<>();
            claims.put("provider", "KAKAO");
            claims.put("userId", user.getId());
            claims.put("role", user.getRole().name());

            String customToken = FirebaseAuth.getInstance()
                    .createCustomToken(firebaseUid, claims);

            logger.info("Firebase Custom Token 생성 완료 - userId: {}", user.getId());
            return customToken;

        } catch (FirebaseAuthException e) {
            logger.error("Firebase Custom Token 생성 실패: {}", e.getMessage());
            throw new IllegalStateException("Firebase 토큰 생성 실패");
        }
    }
}
