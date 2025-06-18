package com.deungsanlog.user.service;

import com.deungsanlog.user.dto.UserCreateRequest;
import com.deungsanlog.user.dto.UserResponse;
import com.deungsanlog.user.dto.UserUpdateRequest;
import com.deungsanlog.user.entity.User;
import com.deungsanlog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * OAuth2 로그인 시 사용자 저장/업데이트
     * Gateway에서 호출하는 메인 메서드
     */
    public UserResponse saveOrUpdateOAuthUser(UserCreateRequest request) {
        log.info("OAuth 사용자 처리 시작: provider={}, providerId={}, email={}",
                request.getProvider(), request.getProviderId(), request.getEmail());

        // provider + providerId로 기존 사용자 찾기
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(
                request.getProvider(),
                request.getProviderId()
        );

        User user;
        if (existingUser.isPresent()) {
            // 기존 사용자 업데이트
            user = existingUser.get();
            updateUserInfo(user, request);
            log.info("기존 사용자 업데이트: userId={}, email={}", user.getId(), user.getEmail());
        } else {
            // 새 사용자 생성
            user = createNewUser(request);
            log.info("새 사용자 생성: email={}", user.getEmail());
        }

        User savedUser = userRepository.save(user);
        log.info("사용자 저장 완료: userId={}", savedUser.getId());

        return convertToUserResponse(savedUser);
    }

    /**
     * 사용자 ID로 조회
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.info("사용자 조회: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        return convertToUserResponse(user);
    }

    /**
     * 🆕 프로필 수정 메서드 (새로 추가!)
     */
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        log.info("프로필 수정 시작: userId={}, nickname={}, profileImgUrl={}",
                userId, request.getNickname(), request.getProfileImgUrl());

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        // 닉네임 수정
        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            String oldNickname = user.getNickname();
            user.setNickname(request.getNickname().trim());
            log.info("닉네임 변경: {} → {}", oldNickname, user.getNickname());
        }

        // 프로필 이미지 URL 수정
        if (request.getProfileImgUrl() != null) {
            String oldProfileImg = user.getProfileImgUrl();
            user.setProfileImgUrl(request.getProfileImgUrl());
            log.info("프로필 이미지 변경: {} → {}", oldProfileImg, user.getProfileImgUrl());
        }

        // 저장 (updatedAt 자동 업데이트)
        User updatedUser = userRepository.save(user);
        log.info("프로필 수정 완료: userId={}", updatedUser.getId());

        return convertToUserResponse(updatedUser);
    }

    /**
     * 이메일로 조회
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.info("이메일로 사용자 조회: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));

        return convertToUserResponse(user);
    }

    /**
     * 기존 사용자 정보 업데이트 (OAuth 전용)
     */
    private void updateUserInfo(User user, UserCreateRequest request) {
        // 이메일이 변경되었을 수 있음
        if (!user.getEmail().equals(request.getEmail())) {
            log.info("이메일 변경: {} → {}", user.getEmail(), request.getEmail());
            user.setEmail(request.getEmail());
        }

        // 닉네임이 변경되었을 수 있음
        if (!user.getNickname().equals(request.getNickname())) {
            log.info("닉네임 변경: {} → {}", user.getNickname(), request.getNickname());
            user.setNickname(request.getNickname());
        }

        // 프로필 이미지가 변경되었을 수 있음
        if (request.getProfileImgUrl() != null &&
                !request.getProfileImgUrl().equals(user.getProfileImgUrl())) {
            log.info("프로필 이미지 변경: {} → {}", user.getProfileImgUrl(), request.getProfileImgUrl());
            user.setProfileImgUrl(request.getProfileImgUrl());
        }
    }

    /**
     * 새 사용자 생성 (OAuth 전용)
     */
    private User createNewUser(UserCreateRequest request) {
        return User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .profileImgUrl(request.getProfileImgUrl())
                .provider(request.getProvider())
                .providerId(request.getProviderId())
                .build();
    }

    /**
     * User 엔티티 → UserResponse DTO 변환
     */
    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImgUrl())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}