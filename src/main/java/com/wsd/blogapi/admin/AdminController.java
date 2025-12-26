package com.wsd.blogapi.admin;

import com.wsd.blogapi.admin.dto.StatisticsResponse;
import com.wsd.blogapi.admin.dto.UpdateUserRequest;
import com.wsd.blogapi.admin.dto.UserManagementResponse;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "통계 조회", description = "전체 통계를 조회합니다")
    @GetMapping("/statistics")
    public StatisticsResponse getStatistics() {
        return adminService.getStatistics();
    }

    @Operation(summary = "사용자 목록 조회", description = "모든 사용자 목록을 조회합니다")
    @GetMapping("/users")
    public Page<UserManagementResponse> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return adminService.getAllUsers(pageable);
    }

    @Operation(summary = "상태별 사용자 조회", description = "특정 상태의 사용자 목록을 조회합니다")
    @GetMapping("/users/status/{status}")
    public Page<UserManagementResponse> getUsersByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return adminService.getUsersByStatus(status, pageable);
    }

    @Operation(summary = "사용자 상세 조회", description = "사용자 상세 정보를 조회합니다")
    @GetMapping("/users/{userId}")
    public UserManagementResponse getUser(@PathVariable Long userId) {
        return adminService.getUser(userId);
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다")
    @PutMapping("/users/{userId}")
    public UserManagementResponse updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return adminService.updateUser(userId, request);
    }

    @Operation(summary = "사용자 정지", description = "사용자를 정지합니다")
    @PostMapping("/users/{userId}/suspend")
    public void suspendUser(@PathVariable Long userId) {
        adminService.suspendUser(userId);
    }

    @Operation(summary = "사용자 정지 해제", description = "사용자 정지를 해제합니다")
    @PostMapping("/users/{userId}/activate")
    public void activateUser(@PathVariable Long userId) {
        adminService.activateUser(userId);
    }

    @Operation(summary = "사용자 삭제", description = "사용자를 완전히 삭제합니다")
    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
    }
}
