package com.wsd.blogapi.category.dto;

import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @Size(min = 1, max = 50, message = "카테고리명은 1~50자 이내여야 합니다")
        String name,

        @Size(min = 1, max = 100, message = "슬러그는 1~100자 이내여야 합니다")
        String slug,

        @Size(max = 200, message = "설명은 200자 이내여야 합니다")
        String description
) {
}
