package com.wsd.blogapi.comment.dto;

import com.wsd.blogapi.comment.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        Long postId,
        String postTitle,
        Long authorId,
        String authorNickname,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getPost().getId(),
                comment.getPost().getTitle(),
                comment.getAuthor().getId(),
                comment.getAuthor().getNickname(),
                comment.getStatus(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
