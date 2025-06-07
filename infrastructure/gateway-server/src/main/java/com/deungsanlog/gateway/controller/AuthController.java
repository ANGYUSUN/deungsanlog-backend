package com.deungsanlog.gateway.controller;

import com.deungsanlog.gateway.service.GoogleOAuthService;
import com.deungsanlog.gateway.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/google/callback")
    public Mono<ResponseEntity<Object>> googleCallback(@RequestParam String code) {
        System.out.println("=== 구글 OAuth 콜백 시작 ===");
        System.out.println("받은 인증 코드: " + code);

        return googleOAuthService.getAccessToken(code)
                .flatMap(accessToken -> {
                    System.out.println("✅ 액세스 토큰 받음: " + accessToken.substring(0, 20) + "...");
                    return googleOAuthService.getUserInfo(accessToken);
                })
                .map(userInfo -> {
                    System.out.println("✅ 구글 사용자 정보: " + userInfo);

                    // JWT 토큰 생성
                    String jwtToken = jwtTokenProvider.generateToken(
                            userInfo.getEmail(),
                            userInfo.getName(),
                            userInfo.getPicture()
                    );

                    System.out.println("✅ JWT 토큰 생성 완료: " + jwtToken.substring(0, 20) + "...");

                    // 응답 데이터 구성
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("token", jwtToken);
                    response.put("user", Map.of(
                            "email", userInfo.getEmail(),
                            "name", userInfo.getName(),
                            "picture", userInfo.getPicture()
                    ));
                    response.put("message", "🎉 로그인 성공!");

                    return ResponseEntity.ok((Object) response);
                })
                .onErrorResume(error -> {
                    System.err.println("❌ OAuth 처리 중 오류: " + error.getMessage());
                    error.printStackTrace();

                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("error", "로그인 처리 중 오류가 발생했습니다.");
                    errorResponse.put("message", error.getMessage());

                    return Mono.just(ResponseEntity.badRequest().body((Object) errorResponse));
                });
    }

    // JWT 토큰 검증 테스트용 엔드포인트
    @GetMapping("/verify")
    public ResponseEntity<Object> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("=== JWT 토큰 검증 시작 ===");
            System.out.println("Authorization 헤더: " + authHeader);

            String token = authHeader.replace("Bearer ", "");

            if (jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                String name = jwtTokenProvider.getNameFromToken(token);

                System.out.println("✅ 토큰 검증 성공 - 사용자: " + email);

                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("email", email);
                response.put("name", name);
                response.put("message", "유효한 토큰입니다.");

                return ResponseEntity.ok((Object) response);
            } else {
                System.out.println("❌ 유효하지 않은 토큰");
                return ResponseEntity.status(401).body((Object) Map.of(
                        "valid", false,
                        "message", "유효하지 않은 토큰입니다."
                ));
            }
        } catch (Exception e) {
            System.err.println("❌ 토큰 검증 실패: " + e.getMessage());
            return ResponseEntity.status(401).body((Object) Map.of(
                    "valid", false,
                    "message", "토큰 검증 중 오류가 발생했습니다."
            ));
        }
    }

    // 로그인 페이지로 리다이렉트하는 간단한 엔드포인트
    @GetMapping("/login")
    public ResponseEntity<Object> login() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "구글 로그인을 시작하세요!");
        response.put("loginUrl", "http://localhost:8080/oauth2/authorization/google");
        response.put("instructions", "위 URL을 클릭하여 구글 로그인을 진행하세요.");

        return ResponseEntity.ok((Object) response);
    }

    // 로그아웃 엔드포인트 (선택사항)
    @PostMapping("/logout")
    public ResponseEntity<Object> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "로그아웃되었습니다. 클라이언트에서 토큰을 삭제하세요.");

        return ResponseEntity.ok((Object) response);
    }
}