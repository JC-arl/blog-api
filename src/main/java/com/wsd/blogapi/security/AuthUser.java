package com.wsd.blogapi.security;

import com.wsd.blogapi.user.User;
import com.wsd.blogapi.user.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthUser implements UserDetails {

    private final Long id;
    private final String email;
    private final UserRole role;
    private final boolean active;

    public AuthUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.active = user.isActive();
    }

    // JWT 토큰에서 생성할 때 사용 (필터에서)
    public AuthUser(Long id, UserRole role) {
        this.id = id;
        this.email = null;
        this.role = role;
        this.active = true; // JWT 발급 시점에 active 검증 완료
    }

    public Long getId() { return id; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() { return null; }

    @Override
    public String getUsername() { return email == null ? String.valueOf(id) : email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return active; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }
}
