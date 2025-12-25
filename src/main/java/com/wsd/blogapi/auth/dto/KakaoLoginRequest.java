package com.wsd.blogapi.auth.dto;

public class KakaoLoginRequest {
    private String kakaoAccessToken;

    public KakaoLoginRequest() {}

    public KakaoLoginRequest(String kakaoAccessToken) {
        this.kakaoAccessToken = kakaoAccessToken;
    }

    public String getKakaoAccessToken() {
        return kakaoAccessToken;
    }

    public void setKakaoAccessToken(String kakaoAccessToken) {
        this.kakaoAccessToken = kakaoAccessToken;
    }
}
