package com.deungsanlog.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NaverUserInfo {

    private String resultcode;
    private String message;
    private Response response;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String id;          // 네이버 고유 ID
        private String email;       // 이메일
        private String name;        // 이름
        private String nickname;    // 닉네임

        @JsonProperty("profile_image")
        private String profileImage; // 프로필 이미지 URL

        private String age;         // 연령대
        private String gender;      // 성별
        private String birthday;    // 생일
        private String birthyear;   // 출생연도
        private String mobile;      // 휴대전화번호
    }
}