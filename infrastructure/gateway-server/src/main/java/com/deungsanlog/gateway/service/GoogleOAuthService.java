package com.deungsanlog.gateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class GoogleOAuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final WebClient webClient;

    public GoogleOAuthService() {
        this.webClient = WebClient.builder().build();
    }

    // 1. 인증 코드로 액세스 토큰 받기
    public Mono<String> getAccessToken(String code) {
        return webClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue(
                        "code=" + code +
                                "&client_id=" + clientId +
                                "&client_secret=" + clientSecret +
                                "&redirect_uri=" + redirectUri +
                                "&grant_type=authorization_code"
                )
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"));
    }

    // 2. 액세스 토큰으로 구글 사용자 정보 가져오기
    public Mono<GoogleUserInfo> getUserInfo(String accessToken) {
        return webClient.get()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GoogleUserInfo.class);
    }

    // 구글 사용자 정보를 담는 DTO 클래스
    public static class GoogleUserInfo {
        private String id;
        private String email;
        private String name;
        private String picture;
        private String given_name;
        private String family_name;

        // 기본 생성자
        public GoogleUserInfo() {}

        // Getter, Setter
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPicture() { return picture; }
        public void setPicture(String picture) { this.picture = picture; }

        public String getGiven_name() { return given_name; }
        public void setGiven_name(String given_name) { this.given_name = given_name; }

        public String getFamily_name() { return family_name; }
        public void setFamily_name(String family_name) { this.family_name = family_name; }

        @Override
        public String toString() {
            return "GoogleUserInfo{" +
                    "id='" + id + '\'' +
                    ", email='" + email + '\'' +
                    ", name='" + name + '\'' +
                    ", picture='" + picture + '\'' +
                    '}';
        }
    }
}