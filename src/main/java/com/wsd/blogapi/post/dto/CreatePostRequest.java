package com.wsd.blogapi.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Size(min = 1, max = 200, message = "제목은 1~200자 이내여야 합니다")
        String title,

        @NotBlank(message = "내용은 필수입니다")
        @Size(min = 1, max = 50000, message = "내용은 1~50000자 이내여야 합니다")
        String content,

        Long categoryId, // 카테고리 ID (선택)

        String status // PUBLISHED or DRAFT (선택, 기본값 PUBLISHED)
) {
    public CreatePostRequest {
        if (status == null || status.isBlank()) {
            status = "PUBLISHED";
        }
    }
}
