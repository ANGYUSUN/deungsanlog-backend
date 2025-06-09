package com.deungsanlog.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    private String email;           // 이메일 (User.email)
    private String nickname;        // 닉네임 (User.nickname)
    private String profileImgUrl;   // 프로필 이미지 URL (User.profileImgUrl)
    private String provider;        // OAuth 제공자 (User.provider) - "google"
    private String providerId;      // OAuth 제공자의 사용자 ID (User.providerId)
}