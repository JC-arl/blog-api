package com.wsd.blogapi.auth;

import com.wsd.blogapi.auth.dto.LoginRequest;
import com.wsd.blogapi.auth.dto.RefreshRequest;
import com.wsd.blogapi.auth.dto.SignupRequest;
import com.wsd.blogapi.auth.dto.TokenResponse;
import com.wsd.blogapi.auth.dto.UpdateProfileRequest;
import com.wsd.blogapi.auth.dto.UserResponse;
import com.wsd.blogapi.common.error.ErrorCode;
import com.wsd.blogapi.user.User;
import com.wsd.blogapi.user.UserRepository;
import com.wsd.blogapi.user.UserRole;
import com.wsd.blogapi.security.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTokenService redisTokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider,
                       RedisTokenService redisTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.redisTokenService = redisTokenService;
    }

    public TokenResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalStateException(ErrorCode.USER_NOT_FOUND.getCode()));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalStateException(ErrorCode.UNAUTHORIZED.getCode());
        }
        if (!user.isActive()) {
            throw new IllegalStateException(ErrorCode.FORBIDDEN.getCode());
        }

        String access = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        String refresh = jwtProvider.createRefreshToken(user.getId(), user.getRole().name());
        redisTokenService.saveRefreshToken(user.getId(), refresh);

        return new TokenResponse(access, refresh);
    }

    public TokenResponse refresh(RefreshRequest req) {
        String refreshToken = req.getRefreshToken();

        Long userId = jwtProvider.getUserId(refreshToken);
        String role = jwtProvider.getRole(refreshToken);

        String saved = redisTokenService.getRefreshToken(userId)
                .orElseThrow(() -> new IllegalStateException(ErrorCode.STATE_CONFLICT.getCode()));

        if (!saved.equals(refreshToken)) {
            throw new IllegalStateException(ErrorCode.STATE_CONFLICT.getCode());
        }

        // access 재발급 (refresh rotate 하고 싶으면 여기서 새 refresh 생성 + 저장)
        String newAccess = jwtProvider.createAccessToken(userId, role);
        return new TokenResponse(newAccess, refreshToken);
    }

    public void logout(Long userId) {
        redisTokenService.deleteRefreshToken(userId);
    }

    @Transactional
    public TokenResponse signup(SignupRequest req) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalStateException(ErrorCode.DUPLICATE_RESOURCE.getCode());
        }

        // 비밀번호 해시
        String passwordHash = passwordEncoder.encode(req.getPassword());

        // 사용자 생성
        User user = new User(
                req.getEmail(),
                passwordHash,
                req.getNickname(),
                UserRole.ROLE_USER,
                "ACTIVE",
                "LOCAL",
                null
        );

        userRepository.save(user);

        // 토큰 발급
        String access = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        String refresh = jwtProvider.createRefreshToken(user.getId(), user.getRole().name());
        redisTokenService.saveRefreshToken(user.getId(), refresh);

        return new TokenResponse(access, refresh);
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException(ErrorCode.USER_NOT_FOUND.getCode()));
        return new UserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException(ErrorCode.USER_NOT_FOUND.getCode()));

        user.updateNickname(request.getNickname());
        userRepository.save(user);

        return new UserResponse(user);
    }
}
