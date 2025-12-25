package com.wsd.blogapi.like.dto;

public record LikeToggleResponse(
        boolean liked,
        Long likeCount,
        String message
) {
    public static LikeToggleResponse liked(Long likeCount) {
        return new LikeToggleResponse(true, likeCount, "좋아요를 눌렀습니다");
    }

    public static LikeToggleResponse unliked(Long likeCount) {
        return new LikeToggleResponse(false, likeCount, "좋아요를 취소했습니다");
    }
}
