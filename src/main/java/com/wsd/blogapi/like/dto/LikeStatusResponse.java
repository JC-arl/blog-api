package com.wsd.blogapi.like.dto;

public record LikeStatusResponse(
        boolean liked,
        Long likeCount
) {
}
