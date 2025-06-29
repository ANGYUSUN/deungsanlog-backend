package com.deungsanlog.gateway.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;                    // DB의 사용자 ID (User.id)
    private String email;               // 이메일 (User.email)
    private String nickname;            // 닉네임 (User.nickname)
    private String profileImgUrl;       // 프로필 이미지 URL (User.profileImgUrl)
    private String provider;            // OAuth 제공자 (User.provider)
    private String providerId;          // OAuth 제공자의 사용자 ID (User.providerId)
    private LocalDateTime createdAt;    // 생성일 (User.createdAt)
    private LocalDateTime updatedAt;    // 수정일 (User.updatedAt)
}