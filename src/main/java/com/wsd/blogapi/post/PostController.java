package com.wsd.blogapi.post;

import com.wsd.blogapi.post.dto.CreatePostRequest;
import com.wsd.blogapi.post.dto.PostResponse;
import com.wsd.blogapi.post.dto.UpdatePostRequest;
import com.wsd.blogapi.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Posts", description = "게시글 API")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 작성합니다")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public PostResponse createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return postService.createPost(request, authUser.getId());
    }

    @Operation(summary = "게시글 목록 조회", description = "공개된 게시글 목록을 페이징하여 조회합니다")
    @GetMapping
    public Page<PostResponse> getPosts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return postService.getPosts(pageable);
    }

    @Operation(summary = "게시글 검색", description = "제목 또는 내용으로 게시글을 검색합니다")
    @GetMapping("/search")
    public Page<PostResponse> searchPosts(
            @RequestParam String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return postService.searchPosts(keyword, pageable);
    }

    @Operation(summary = "카테고리별 게시글 조회", description = "특정 카테고리의 게시글을 조회합니다")
    @GetMapping("/category/{categoryId}")
    public Page<PostResponse> getPostsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return postService.getPostsByCategory(categoryId, pageable);
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 상세 정보를 조회합니다 (조회수 증가)")
    @GetMapping("/{id}")
    public PostResponse getPost(@PathVariable Long id) {
        return postService.getPost(id);
    }

    @Operation(summary = "내 게시글 목록", description = "로그인한 사용자의 모든 게시글을 조회합니다")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Page<PostResponse> getMyPosts(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return postService.getMyPosts(authUser.getId(), pageable);
    }

    @Operation(summary = "게시글 수정", description = "게시글 정보를 수정합니다")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PostResponse updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return postService.updatePost(id, request, authUser.getId());
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 소프트 삭제합니다")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        postService.deletePost(id, authUser.getId());
    }

    @Operation(summary = "게시글 강제 삭제 (관리자)", description = "게시글을 DB에서 완전히 삭제합니다")
    @DeleteMapping("/{id}/force")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void forceDeletePost(@PathVariable Long id) {
        postService.forceDeletePost(id);
    }
}
