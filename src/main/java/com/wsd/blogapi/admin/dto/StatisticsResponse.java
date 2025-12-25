package com.wsd.blogapi.admin.dto;

public record StatisticsResponse(
        Long totalUsers,
        Long activeUsers,
        Long suspendedUsers,
        Long totalPosts,
        Long publishedPosts,
        Long draftPosts,
        Long deletedPosts,
        Long totalComments,
        Long activeComments,
        Long totalLikes,
        Long totalCategories
) {
}
