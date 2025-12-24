package com.wsd.blogapi.oauth;

import com.wsd.blogapi.security.AuthUser;
import com.wsd.blogapi.user.User;
import com.wsd.blogapi.user.UserRepository;
import com.wsd.blogapi.user.UserRole;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public OAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // google, kakao
        Map<String, Object> attributes = oauth2User.getAttributes();

        String providerId;
        String email;
        String nickname;

        if ("google".equals(registrationId)) {
            providerId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            nickname = (String) attributes.get("name");
        } else if ("kakao".equals(registrationId)) {
            providerId = String.valueOf(attributes.get("id"));
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            nickname = (String) profile.get("nickname");
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        // 이메일로 사용자 찾기
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 신규 사용자 생성
                    User newUser = new User(
                            email,
                            null, // OAuth2 사용자는 비밀번호 없음
                            nickname,
                            UserRole.ROLE_USER,
                            "ACTIVE",
                            registrationId.toUpperCase(), // GOOGLE, KAKAO
                            providerId
                    );
                    return userRepository.save(newUser);
                });

        return new AuthUser(user);
    }
}
