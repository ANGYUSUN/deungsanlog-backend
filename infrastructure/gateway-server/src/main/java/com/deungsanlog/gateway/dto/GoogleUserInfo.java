package com.deungsanlog.gateway.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserInfo {

    private String id;          // Google 고유 ID
    private String email;       // 이메일
    private String name;        // 이름
    private String picture;     // 프로필 이미지 URL
    private String givenName;   // 이름 (선택사항)
    private String familyName;  // 성 (선택사항)
    private String locale;      // 언어 설정 (선택사항)
}