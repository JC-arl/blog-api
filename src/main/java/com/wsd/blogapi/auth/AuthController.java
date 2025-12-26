package com.wsd.blogapi.auth;

import com.wsd.blogapi.auth.dto.*;
import com.wsd.blogapi.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증/인가 API")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final KakaoAuthService kakaoAuthService;

    public AuthController(AuthService authService, KakaoAuthService kakaoAuthService) {
        this.authService = authService;
        this.kakaoAuthService = kakaoAuthService;
    }

    @Operation(
            summary = "회원가입",
            description = "이메일과 비밀번호로 새로운 계정을 생성합니다. 회원가입 시 자동으로 JWT 토큰이 발급됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이메일 중복, 유효성 검증 실패 등)"),
            @ApiResponse(responseCode = "422", description = "처리할 수 없는 엔티티 (이메일 중복 등)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse signup(@Valid @RequestBody SignupRequest req) {
        return authService.signup(req);
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인합니다. Access Token과 Refresh Token을 발급받습니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"email\": \"admin@blog.com\", \"password\": \"admin1234\"}"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 오류)"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @Operation(
            summary = "토큰 갱신",
            description = "Refresh Token으로 새로운 Access Token을 발급받습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req);
    }

    @Operation(
            summary = "로그아웃",
            description = "현재 로그인된 사용자를 로그아웃합니다. Refresh Token이 무효화됩니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal AuthUser user) {
        authService.logout(user.getId());
    }

    @Operation(
            summary = "카카오 로그인",
            description = "카카오 Access Token으로 로그인하고 Firebase Custom Token을 발급받습니다.",
            hidden = true
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카카오 로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 카카오 토큰"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "422", description = "처리할 수 없는 엔티티"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/kakao-login")
    public FirebaseTokenResponse kakaoLogin(@Valid @RequestBody KakaoLoginRequest req) {
        String firebaseCustomToken = kakaoAuthService.processKakaoLogin(req.getKakaoAccessToken());
        return new FirebaseTokenResponse(firebaseCustomToken);
    }

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 정보를 조회합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal AuthUser user) {
        return authService.getMe(user.getId());
    }

    @Operation(
            summary = "내 정보 수정",
            description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "422", description = "처리할 수 없는 엔티티"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/me")
    public UserResponse updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal AuthUser user
    ) {
        return authService.updateProfile(user.getId(), request);
    }
}
