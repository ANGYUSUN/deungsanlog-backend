package com.deungsanlog.gateway.controller;

import com.deungsanlog.gateway.component.JwtTokenProvider;
import com.deungsanlog.gateway.dto.GoogleTokenResponse;
import com.deungsanlog.gateway.dto.GoogleUserInfo;
import com.deungsanlog.gateway.service.UserServiceClient;
import com.deungsanlog.gateway.dto.UserCreateRequest;
import com.deungsanlog.gateway.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserServiceClient userServiceClient;
    private final WebClient webClient = WebClient.builder().build();

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    /**
     * Google OAuth2 로그인 시작점
     * 프론트엔드에서 이 URL로 리다이렉트하면 Google 로그인 페이지로 이동
     */
    @GetMapping("/google")
    public Mono<ResponseEntity<Map<String, String>>> googleLogin() {
        log.info("Google OAuth2 로그인 시작");

        String googleAuthUrl = "https://accounts.google.com/o/oauth2/auth"
                + "?client_id=" + googleClientId
                + "&redirect_uri=" + redirectUri
                + "&scope=email profile"
                + "&response_type=code"
                + "&access_type=offline";

        log.info("Google 인증 URL 생성: {}", googleAuthUrl);

        Map<String, String> response = Map.of(
                "authUrl", googleAuthUrl,
                "message", "Google 로그인 페이지로 리다이렉트하세요"
        );

        return Mono.just(ResponseEntity.ok(response));
    }

    /**
     * Google OAuth2 콜백 처리
     * Google에서 인증 완료 후 이 엔드포인트로 code와 함께 리다이렉트됨
     */
    @GetMapping("/google/callback")
    public Mono<ResponseEntity<Map<String, Object>>> googleCallback(@RequestParam String code) {
        log.info("Google OAuth2 콜백 처리 시작: code={}", code);

        return exchangeCodeForToken(code)
                .flatMap(this::getUserInfoFromGoogle)
                .flatMap(this::saveUserToUserService)
                .flatMap(this::generateJwtResponse)
                .doOnSuccess(response -> log.info("OAuth2 로그인 성공"))
                .onErrorResume(error -> {
                    log.error("OAuth2 로그인 실패", error);
                    Map<String, Object> errorResponse = Map.of(
                            "success", false,
                            "error", "OAuth2 로그인 실패",
                            "message", error.getMessage()
                    );
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                });
    }

    /**
     * Step 1: Google에서 Authorization Code를 Access Token으로 교환
     */
    private Mono<String> exchangeCodeForToken(String code) {
        log.info("Google Access Token 요청 시작");

        String tokenUrl = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", googleClientId);
        formData.add("client_secret", googleClientSecret);
        formData.add("code", code);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", redirectUri);

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .map(GoogleTokenResponse::getAccessToken)
                .doOnSuccess(token -> log.info("Google Access Token 획득 성공"));
    }

    /**
     * Step 2: Access Token으로 Google에서 사용자 정보 조회
     */
    private Mono<GoogleUserInfo> getUserInfoFromGoogle(String accessToken) {
        log.info("Google 사용자 정보 요청 시작");

        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        return webClient.get()
                .uri(userInfoUrl)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GoogleUserInfo.class)
                .doOnSuccess(userInfo -> log.info("Google 사용자 정보 획득 성공: email={}", userInfo.getEmail()));
    }

    /**
     * Step 3: User Service에 사용자 정보 저장/업데이트
     */
    private Mono<UserResponse> saveUserToUserService(GoogleUserInfo googleUser) {
        log.info("User Service에 사용자 정보 저장 시작: email={}", googleUser.getEmail());

        UserCreateRequest request = UserCreateRequest.builder()
                .email(googleUser.getEmail())
                .nickname(googleUser.getName())
                .profileImgUrl(googleUser.getPicture())
                .provider("google")
                .providerId(googleUser.getId())
                .build();

        return userServiceClient.saveOrUpdateUser(request);
    }

    /**
     * Step 4: JWT 토큰 생성 후 응답
     */
    private Mono<ResponseEntity<Map<String, Object>>> generateJwtResponse(UserResponse user) {
        log.info("JWT 토큰 생성 시작: userId={}", user.getId());

        try {
            // 멘토님의 JwtTokenProvider 사용
            String jwtToken = jwtTokenProvider.generateToken(
                    user.getEmail(),
                    List.of("ROLE_USER"), // 기본 권한
                    user.getId()
            );

            Map<String, Object> response = Map.of(
                    "success", true,
                    "token", jwtToken,
                    "user", Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "nickname", user.getNickname(),
                            "profileImgUrl", user.getProfileImgUrl() != null ? user.getProfileImgUrl() : ""
                    )
            );

            log.info("JWT 토큰 생성 완료: userId={}", user.getId());
            return Mono.just(ResponseEntity.ok(response));

        } catch (Exception e) {
            log.error("JWT 토큰 생성 실패", e);
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "error", "JWT 토큰 생성 실패",
                    "message", e.getMessage()
            );
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
        }
    }

    /**
     * 토큰 검증 엔드포인트 (클라이언트에서 토큰 유효성 확인용)
     */
    @GetMapping("/verify")
    public Mono<ResponseEntity<Map<String, Object>>> verifyToken(@RequestHeader("X-AUTH-TOKEN") String token) {
        log.info("토큰 검증 요청");

        try {
            if (!jwtTokenProvider.validateToken(token)) {
                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "message", "Invalid token")));
            }

            String email = jwtTokenProvider.getUserEmail(token);
            String role = jwtTokenProvider.getUserRole(token);
            Long userId = jwtTokenProvider.getUserIdAsLong(token);

            Map<String, Object> response = Map.of(
                    "valid", true,
                    "user", Map.of(
                            "id", userId,
                            "email", email,
                            "role", role
                    )
            );

            return Mono.just(ResponseEntity.ok(response));

        } catch (Exception e) {
            log.error("토큰 검증 실패", e);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "Token verification failed")));
        }
    }
}