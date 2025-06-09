package com.deungsanlog.gateway.service;

import com.deungsanlog.gateway.dto.UserCreateRequest;
import com.deungsanlog.gateway.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserServiceClient {

    private final WebClient webClient;
    private final String userServiceUrl;

    public UserServiceClient(@Value("${services.user-service.url:http://localhost:8081}") String userServiceUrl) {
        this.userServiceUrl = userServiceUrl;
        this.webClient = WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    /**
     * User Service에 사용자 정보 저장/업데이트 요청
     */
    public Mono<UserResponse> saveOrUpdateUser(UserCreateRequest request) {
        log.info("User Service 호출: POST /api/users/oauth - email={}", request.getEmail());

        return webClient.post()
                .uri("/api/users/oauth")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .doOnSuccess(response -> log.info("User Service 응답 성공: userId={}", response.getId()))
                .doOnError(error -> log.error("User Service 호출 실패: {}", error.getMessage()));
    }

    /**
     * 사용자 ID로 사용자 정보 조회
     */
    public Mono<UserResponse> getUserById(Long userId) {
        log.info("User Service 호출: GET /api/users/{}", userId);

        return webClient.get()
                .uri("/api/users/{userId}", userId)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .doOnSuccess(response -> log.info("사용자 조회 성공: email={}", response.getEmail()))
                .doOnError(error -> log.error("사용자 조회 실패: userId={}, error={}", userId, error.getMessage()));
    }

    /**
     * 이메일로 사용자 정보 조회
     */
    public Mono<UserResponse> getUserByEmail(String email) {
        log.info("User Service 호출: GET /api/users/email/{}", email);

        return webClient.get()
                .uri("/api/users/email/{email}", email)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .doOnSuccess(response -> log.info("이메일로 사용자 조회 성공: userId={}", response.getId()))
                .doOnError(error -> log.error("이메일로 사용자 조회 실패: email={}, error={}", email, error.getMessage()));
    }
}