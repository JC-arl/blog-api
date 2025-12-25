package com.wsd.blogapi.oauth;

import com.wsd.blogapi.auth.RedisTokenService;
import com.wsd.blogapi.security.AuthUser;
import com.wsd.blogapi.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final RedisTokenService redisTokenService;
    private final String frontendUrl;

    public OAuth2SuccessHandler(JwtProvider jwtProvider,
                                RedisTokenService redisTokenService,
                                @Value("${app.frontend-url}") String frontendUrl) {
        this.jwtProvider = jwtProvider;
        this.redisTokenService = redisTokenService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        String accessToken = jwtProvider.createAccessToken(authUser.getId(), authUser.getAuthorities().iterator().next().getAuthority());
        String refreshToken = jwtProvider.createRefreshToken(authUser.getId(), authUser.getAuthorities().iterator().next().getAuthority());

        redisTokenService.saveRefreshToken(authUser.getId(), refreshToken);

        // 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        // 실제 운영에서는 쿠키나 다른 방법 사용 권장
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
