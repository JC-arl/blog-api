package com.wsd.blogapi.like;

import com.wsd.blogapi.like.dto.LikeStatusResponse;
import com.wsd.blogapi.like.dto.LikeToggleResponse;
import com.wsd.blogapi.like.dto.PostLikeResponse;
import com.wsd.blogapi.post.dto.PostResponse;
import com.wsd.blogapi.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Likes", description = "좋아요 API")
@RestController
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @Operation(summary = "좋아요 토글", description = "게시글 좋아요/좋아요 취소")
    @PostMapping("/posts/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public LikeToggleResponse toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return postLikeService.toggleLike(postId, authUser.getId());
    }

    @Operation(summary = "좋아요 상태 확인", description = "사용자가 해당 게시글에 좋아요를 눌렀는지 확인")
    @GetMapping("/posts/{postId}/like/status")
    @PreAuthorize("isAuthenticated()")
    public LikeStatusResponse getLikeStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return postLikeService.getLikeStatus(postId, authUser.getId());
    }

    @Operation(summary = "좋아요 수 조회", description = "게시글의 좋아요 수를 조회")
    @GetMapping("/posts/{postId}/like/count")
    public Long getLikeCount(@PathVariable Long postId) {
        return postLikeService.getLikeCount(postId);
    }

    @Operation(summary = "좋아요한 사용자 목록", description = "게시글에 좋아요를 누른 사용자 목록")
    @GetMapping("/posts/{postId}/likes")
    public Page<PostLikeResponse> getLikesByPost(
            @PathVariable Long postId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return postLikeService.getLikesByPost(postId, pageable);
    }

    @Operation(summary = "내가 좋아요한 게시글", description = "로그인한 사용자가 좋아요한 게시글 목록")
    @GetMapping("/likes/my")
    @PreAuthorize("isAuthenticated()")
    public Page<PostResponse> getMyLikedPosts(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return postLikeService.getMyLikedPosts(authUser.getId(), pageable);
    }
}
