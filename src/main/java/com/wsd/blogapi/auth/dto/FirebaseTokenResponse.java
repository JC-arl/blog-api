package com.wsd.blogapi.auth.dto;

public class FirebaseTokenResponse {
    private String firebaseCustomToken;

    public FirebaseTokenResponse() {}

    public FirebaseTokenResponse(String firebaseCustomToken) {
        this.firebaseCustomToken = firebaseCustomToken;
    }

    public String getFirebaseCustomToken() {
        return firebaseCustomToken;
    }

    public void setFirebaseCustomToken(String firebaseCustomToken) {
        this.firebaseCustomToken = firebaseCustomToken;
    }
}
