package com.wsd.blogapi.auth.dto;

public class KakaoUserInfo {
    private final Long kakaoId;
    private final String email;
    private final String nickname;

    public KakaoUserInfo(Long kakaoId, String email, String nickname) {
        this.kakaoId = kakaoId;
        this.email = email;
        this.nickname = nickname;
    }

    public Long getKakaoId() {
        return kakaoId;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }
}
