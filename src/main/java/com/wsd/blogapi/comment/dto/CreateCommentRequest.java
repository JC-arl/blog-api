package com.wsd.blogapi.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "댓글 내용은 필수입니다")
        @Size(min = 1, max = 1000, message = "댓글은 1~1000자 이내여야 합니다")
        String content
) {
}
