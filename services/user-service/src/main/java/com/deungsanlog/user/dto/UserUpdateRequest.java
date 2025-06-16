package com.deungsanlog.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String nickname;        // 수정할 닉네임
    private String profileImgUrl;   // 수정할 프로필 이미지 URL
}