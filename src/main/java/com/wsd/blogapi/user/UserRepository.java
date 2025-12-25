package com.wsd.blogapi.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // 관리자 기능
    Page<User> findByStatus(String status, Pageable pageable);
    Long countByStatus(String status);
}