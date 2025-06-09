package com.deungsanlog.user.repository;

import com.deungsanlog.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * OAuth 제공자와 제공자 ID로 사용자 찾기
     * Google 로그인 시 기존 사용자인지 확인용
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    /**
     * 이메일로 사용자 찾기
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 제공자와 제공자 ID 존재 여부 확인
     */
    boolean existsByProviderAndProviderId(String provider, String providerId);
}