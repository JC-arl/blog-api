package com.wsd.blogapi.post.dto;

import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
        @Size(min = 1, max = 200, message = "제목은 1~200자 이내여야 합니다")
        String title,

        @Size(min = 1, max = 50000, message = "내용은 1~50000자 이내여야 합니다")
        String content,

        Long categoryId, // 카테고리 ID (null 가능)

        String status // PUBLISHED, DRAFT, DELETED
) {
}
