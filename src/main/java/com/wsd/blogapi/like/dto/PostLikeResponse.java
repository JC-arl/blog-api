package com.wsd.blogapi.like.dto;

import com.wsd.blogapi.like.PostLike;

import java.time.LocalDateTime;

public record PostLikeResponse(
        Long id,
        Long postId,
        String postTitle,
        Long userId,
        String userNickname,
        LocalDateTime createdAt
) {
    public static PostLikeResponse from(PostLike postLike) {
        return new PostLikeResponse(
                postLike.getId(),
                postLike.getPost().getId(),
                postLike.getPost().getTitle(),
                postLike.getUser().getId(),
                postLike.getUser().getNickname(),
                postLike.getCreatedAt()
        );
    }
}
