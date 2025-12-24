package com.wsd.blogapi.auth;

import com.wsd.blogapi.auth.dto.LoginRequest;
import com.wsd.blogapi.auth.dto.RefreshRequest;
import com.wsd.blogapi.auth.dto.SignupRequest;
import com.wsd.blogapi.auth.dto.TokenResponse;
import com.wsd.blogapi.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse signup(@Valid @RequestBody SignupRequest req) {
        return authService.signup(req);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req);
    }

    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal AuthUser user) {
        authService.logout(user.getId());
    }
}
