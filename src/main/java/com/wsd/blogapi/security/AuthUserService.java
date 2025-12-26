package com.wsd.blogapi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import com.wsd.blogapi.user.User;
import com.wsd.blogapi.user.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthUserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) { // username=email
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("USER_NOT_FOUND"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash() == null ? "" : user.getPasswordHash())
                .authorities(new SimpleGrantedAuthority(user.getRole().name()))
                .build();
    }
}