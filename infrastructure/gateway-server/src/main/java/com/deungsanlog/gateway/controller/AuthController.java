package com.deungsanlog.gateway.controller;

import com.deungsanlog.gateway.component.JwtTokenProvider;
import com.deungsanlog.gateway.dto.*;
import com.deungsanlog.gateway.service.UserServiceClient;
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

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private final String googleRedirectUri = "http://localhost:8080/auth/google/callback";
    private final String naverRedirectUri = "http://localhost:8080/auth/naver/callback";
    // Google 설정
    @Value("${google.oauth.client-id}")
    private String googleClientId;
    @Value("${google.oauth.client-secret}")
    private String googleClientSecret;
    // 네이버 설정
    @Value("${naver.oauth.client-id}")
    private String naverClientId;
    @Value("${naver.oauth.client-secret}")
    private String naverClientSecret;

    /**
     * Google OAuth2 로그인 시작점 - 바로 Google로 리다이렉트
     */
    @GetMapping("/google")
    public Mono<ResponseEntity<Void>> googleLogin() {
        log.info("Google OAuth2 로그인 시작 - 바로 리다이렉트");

        String googleAuthUrl = "https://accounts.google.com/o/oauth2/auth"
                + "?client_id=" + googleClientId
                + "&redirect_uri=" + googleRedirectUri
                + "&scope=email%20profile"  // 공백을 %20으로 인코딩
                + "&response_type=code"
                + "&access_type=offline";

        log.info("Google 인증 URL로 리다이렉트: {}", googleAuthUrl);

        // 바로 Google 로그인 페이지로 리다이렉트
        return Mono.just(ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(googleAuthUrl))
                .build());
    }

    /**
     * 네이버 OAuth2 로그인 시작점 - 바로 네이버로 리다이렉트
     */
    @GetMapping("/naver")
    public Mono<ResponseEntity<Void>> naverLogin() {
        log.info("네이버 OAuth2 로그인 시작 - 바로 리다이렉트");

        String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize"
                + "?client_id=" + naverClientId
                + "&redirect_uri=" + naverRedirectUri
                + "&scope=name%20email"  // 공백을 %20으로 인코딩
                + "&response_type=code"
                + "&state=RANDOM_STATE_STRING"; // 보안을 위한 state 파라미터

        log.info("네이버 인증 URL로 리다이렉트: {}", naverAuthUrl);

        // 바로 네이버 로그인 페이지로 리다이렉트
        return Mono.just(ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(naverAuthUrl))
                .build());
    }

    /**
     * Google OAuth2 콜백 처리 - 프론트엔드로 리다이렉트
     */
    @GetMapping("/google/callback")
    public Mono<ResponseEntity<Void>> googleCallback(@RequestParam String code) {
        log.info("Google OAuth2 콜백 처리 시작: code={}", code);

        return exchangeGoogleCodeForToken(code)
                .flatMap(this::getGoogleUserInfo)
                .flatMap(this::saveGoogleUserToUserService)
                .map(userResponse -> {
                    try {
                        // JWT 토큰 생성
                        String jwtToken = jwtTokenProvider.generateToken(
                                userResponse.getEmail(),
                                List.of("ROLE_USER"),
                                userResponse.getId()
                        );

                        // 프론트엔드로 토큰과 함께 리다이렉트
                        String redirectUrl = "http://localhost:5173/login?token=" + jwtToken;

                        return ResponseEntity.status(HttpStatus.FOUND)
                                .location(URI.create(redirectUrl))
                                .<Void>build();
                    } catch (Exception e) {
                        log.error("JWT 토큰 생성 실패", e);
                        String errorRedirectUrl = "http://localhost:5173/login?error=" +
                                URLEncoder.encode("JWT 토큰 생성 실패", StandardCharsets.UTF_8);

                        return ResponseEntity.status(HttpStatus.FOUND)
                                .location(URI.create(errorRedirectUrl))
                                .<Void>build();
                    }
                })
                .onErrorResume(error -> {
                    log.error("Google OAuth2 로그인 실패", error);
                    String errorRedirectUrl = "http://localhost:5173/login?error=" +
                            URLEncoder.encode(error.getMessage(), StandardCharsets.UTF_8);

                    ResponseEntity<Void> errorResponse = ResponseEntity.status(HttpStatus.FOUND)
                            .location(URI.create(errorRedirectUrl))
                            .build();

                    return Mono.just(errorResponse);
                });
    }

    /**
     * 네이버 OAuth2 콜백 처리 - 프론트엔드로 리다이렉트
     */
    @GetMapping("/naver/callback")
    public Mono<ResponseEntity<Void>> naverCallback(
            @RequestParam String code,
            @RequestParam String state) {
        log.info("네이버 OAuth2 콜백 처리 시작: code={}, state={}", code, state);

        return exchangeNaverCodeForToken(code, state)
                .flatMap(this::getNaverUserInfo)
                .flatMap(this::saveNaverUserToUserService)
                .map(userResponse -> {
                    try {
                        // JWT 토큰 생성
                        String jwtToken = jwtTokenProvider.generateToken(
                                userResponse.getEmail(),
                                List.of("ROLE_USER"),
                                userResponse.getId()
                        );

                        // 프론트엔드로 토큰과 함께 리다이렉트
                        String redirectUrl = "http://localhost:5173/login?token=" + jwtToken;

                        return ResponseEntity.status(HttpStatus.FOUND)
                                .location(URI.create(redirectUrl))
                                .<Void>build();
                    } catch (Exception e) {
                        log.error("JWT 토큰 생성 실패", e);
                        String errorRedirectUrl = "http://localhost:5173/login?error=" +
                                URLEncoder.encode("JWT 토큰 생성 실패", StandardCharsets.UTF_8);

                        return ResponseEntity.status(HttpStatus.FOUND)
                                .location(URI.create(errorRedirectUrl))
                                .<Void>build();
                    }
                })
                .onErrorResume(error -> {
                    log.error("네이버 OAuth2 로그인 실패", error);
                    String errorRedirectUrl = "http://localhost:5173/login?error=" +
                            URLEncoder.encode(error.getMessage(), StandardCharsets.UTF_8);

                    ResponseEntity<Void> errorResponse = ResponseEntity.status(HttpStatus.FOUND)
                            .location(URI.create(errorRedirectUrl))
                            .build();

                    return Mono.just(errorResponse);
                });
    }

    // Google 관련 메서드들
    private Mono<String> exchangeGoogleCodeForToken(String code) {
        log.info("Google Access Token 요청 시작");

        String tokenUrl = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", googleClientId);
        formData.add("client_secret", googleClientSecret);
        formData.add("code", code);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", googleRedirectUri);

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .map(GoogleTokenResponse::getAccessToken)
                .doOnSuccess(token -> log.info("Google Access Token 획득 성공"));
    }

    private Mono<GoogleUserInfo> getGoogleUserInfo(String accessToken) {
        log.info("Google 사용자 정보 요청 시작");

        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        return webClient.get()
                .uri(userInfoUrl)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GoogleUserInfo.class)
                .doOnSuccess(userInfo -> log.info("Google 사용자 정보 획득 성공: email={}", userInfo.getEmail()));
    }

    private Mono<UserResponse> saveGoogleUserToUserService(GoogleUserInfo googleUser) {
        log.info("User Service에 Google 사용자 정보 저장 시작: email={}", googleUser.getEmail());

        UserCreateRequest request = UserCreateRequest.builder()
                .email(googleUser.getEmail())
                .nickname(googleUser.getName())
                .profileImgUrl(googleUser.getPicture())
                .provider("google")
                .providerId(googleUser.getId())
                .build();

        return userServiceClient.saveOrUpdateUser(request);
    }

    // 네이버 관련 메서드들
    private Mono<String> exchangeNaverCodeForToken(String code, String state) {
        log.info("네이버 Access Token 요청 시작");

        String tokenUrl = "https://nid.naver.com/oauth2.0/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", naverClientId);
        formData.add("client_secret", naverClientSecret);
        formData.add("code", code);
        formData.add("state", state);
        formData.add("grant_type", "authorization_code");

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(NaverTokenResponse.class)
                .map(NaverTokenResponse::getAccessToken)
                .doOnSuccess(token -> log.info("네이버 Access Token 획득 성공"));
    }

    private Mono<NaverUserInfo> getNaverUserInfo(String accessToken) {
        log.info("네이버 사용자 정보 요청 시작");

        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";

        return webClient.get()
                .uri(userInfoUrl)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(NaverUserInfo.class)
                .doOnSuccess(userInfo -> log.info("네이버 사용자 정보 획득 성공: email={}",
                        userInfo.getResponse().getEmail()));
    }

    private Mono<UserResponse> saveNaverUserToUserService(NaverUserInfo naverUser) {
        NaverUserInfo.Response response = naverUser.getResponse();
        log.info("User Service에 네이버 사용자 정보 저장 시작: email={}", response.getEmail());

        UserCreateRequest request = UserCreateRequest.builder()
                .email(response.getEmail())
                .nickname(response.getNickname() != null ? response.getNickname() : response.getName())
                .profileImgUrl(response.getProfileImage())
                .provider("naver")
                .providerId(response.getId())
                .build();

        return userServiceClient.saveOrUpdateUser(request);
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