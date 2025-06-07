package com.deungsanlog.gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println(" JWT 필터 실행!!");

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        System.out.println("=== JWT 필터 실행 ===");
        System.out.println("요청 경로: " + path);
        System.out.println("요청 방법: " + request.getMethod());
        System.out.println("전체 URI: " + request.getURI());

        // 인증이 필요 없는 경로들
        if (isPublicPath(path)) {
            System.out.println("✅ 공개 경로 - 인증 생략: " + path);
            return chain.filter(exchange);
        }

        // Authorization 헤더 확인
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println(" Authorization 헤더 없음 또는 잘못된 형식");
            System.out.println("현재 Authorization 헤더: " + authHeader);
            return handleUnauthorized(exchange);
        }

        // JWT 토큰 추출
        String token = authHeader.substring(7); // "Bearer " 제거
        System.out.println("JWT 토큰 추출: " + token.substring(0, Math.min(20, token.length())) + "...");

        try {
            // JWT 토큰 검증
            if (!jwtTokenProvider.validateToken(token)) {
                System.out.println(" JWT 토큰 검증 실패");
                return handleUnauthorized(exchange);
            }

            // 토큰에서 사용자 정보 추출
            String userEmail = jwtTokenProvider.getEmailFromToken(token);
            String userName = jwtTokenProvider.getNameFromToken(token);

            System.out.println(" JWT 토큰 검증 성공 - 사용자: " + userEmail);

            // 요청 헤더에 사용자 정보 추가
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Email", userEmail)
                    .header("X-User-Name", userName)
                    .header("X-Authenticated", "true")
                    .build();

            System.out.println("사용자 정보 헤더 추가 완료");
            System.out.println("  - X-User-Email: " + userEmail);
            System.out.println("  - X-User-Name: " + userName);

            // 수정된 요청으로 다음 필터 체인 실행
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            System.err.println("JWT 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
            return handleUnauthorized(exchange);
        }
    }

    // 인증이 필요 없는 공개 경로 확인
    private boolean isPublicPath(String path) {
        boolean isPublic = path.startsWith("/auth/") ||           // 인증 관련 경로
                path.startsWith("/oauth2/") ||         // OAuth2 경로
                path.startsWith("/actuator/") ||       // 헬스체크
                path.equals("/") ||                    // 루트 경로
                path.startsWith("/public/") ||         // 공개 API
                path.startsWith("/health") ||          // 헬스체크
                path.contains("/favicon.ico") ||       // 파비콘
                path.startsWith("/fallback/");         // Fallback 경로 추가

        System.out.println("경로 공개 여부 체크: " + path + " → " + (isPublic ? "공개" : "보호됨"));
        return isPublic;
    }

    // 인증 실패 처리
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        System.out.println("인증 실패 - 401 응답 반환");

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        // JSON 응답 설정
        response.getHeaders().add("Content-Type", "application/json");

        String body = "{"
                + "\"success\": false,"
                + "\"message\": \"인증이 필요합니다. 유효한 JWT 토큰을 제공해주세요.\","
                + "\"error\": \"Unauthorized\","
                + "\"timestamp\": \"" + System.currentTimeMillis() + "\""
                + "}";

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }

    @Override
    public int getOrder() {
        return -1; // 다른 필터들보다 먼저 실행
    }
}