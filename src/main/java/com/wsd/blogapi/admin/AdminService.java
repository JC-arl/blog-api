package com.wsd.blogapi.admin;

import com.wsd.blogapi.admin.dto.StatisticsResponse;
import com.wsd.blogapi.admin.dto.UpdateUserRequest;
import com.wsd.blogapi.admin.dto.UserManagementResponse;
import com.wsd.blogapi.category.CategoryRepository;
import com.wsd.blogapi.comment.CommentRepository;
import com.wsd.blogapi.like.PostLikeRepository;
import com.wsd.blogapi.post.PostRepository;
import com.wsd.blogapi.user.User;
import com.wsd.blogapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 전체 통계 조회
     */
    public StatisticsResponse getStatistics() {
        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countByStatus("ACTIVE");
        Long suspendedUsers = userRepository.countByStatus("SUSPENDED");

        Long totalPosts = postRepository.count();
        Long publishedPosts = postRepository.countByStatus("PUBLISHED");
        Long draftPosts = postRepository.countByStatus("DRAFT");
        Long deletedPosts = postRepository.countByStatus("DELETED");

        Long totalComments = commentRepository.count();
        Long activeComments = commentRepository.countByStatus("ACTIVE");

        Long totalLikes = postLikeRepository.count();
        Long totalCategories = categoryRepository.count();

        return new StatisticsResponse(
                totalUsers,
                activeUsers,
                suspendedUsers,
                totalPosts,
                publishedPosts,
                draftPosts,
                deletedPosts,
                totalComments,
                activeComments,
                totalLikes,
                totalCategories
        );
    }

    /**
     * 모든 사용자 목록 조회
     */
    public Page<UserManagementResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserManagementResponse::from);
    }

    /**
     * 상태별 사용자 목록 조회
     */
    public Page<UserManagementResponse> getUsersByStatus(String status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable)
                .map(UserManagementResponse::from);
    }

    /**
     * 사용자 상세 조회
     */
    public UserManagementResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        return UserManagementResponse.from(user);
    }

    /**
     * 사용자 정보 수정
     */
    @Transactional
    public UserManagementResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        if (request.nickname() != null) {
            user.updateNickname(request.nickname());
        }

        if (request.role() != null) {
            user.updateRole(request.role());
        }

        if (request.status() != null) {
            if ("ACTIVE".equals(request.status())) {
                user.activate();
            } else if ("SUSPENDED".equals(request.status())) {
                user.suspend();
            }
        }

        return UserManagementResponse.from(user);
    }

    /**
     * 사용자 정지
     */
    @Transactional
    public void suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        user.suspend();
    }

    /**
     * 사용자 정지 해제
     */
    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        user.activate();
    }

    /**
     * 사용자 강제 삭제 (완전 삭제)
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        userRepository.delete(user);
    }
}
