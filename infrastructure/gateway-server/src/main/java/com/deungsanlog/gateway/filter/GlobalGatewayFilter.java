package com.deungsanlog.gateway.filter;

import com.deungsanlog.gateway.component.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalGatewayFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String routeId = Optional.ofNullable(route).isPresent() ? route.getId() : "UNKNOWN_ROUTE";

        log.info("GlobalGatewayFilter 실행: routeId={}, path={}", routeId, request.getPath());

        // 인증이 필요 없는 경로 체크
        String path = request.getPath().value();
        if (isPublicPath(path)) {
            log.info("공개 경로 접근: {}", path);
            return chain.filter(exchange);
        }

        // [STEP-01] Header에 X-AUTH-TOKEN 검증
        String authToken = request.getHeaders().getFirst("X-AUTH-TOKEN");
        if (authToken == null || authToken.isEmpty()) {
            log.warn("X-AUTH-TOKEN 헤더가 없습니다");
            return onError(exchange, "Missing X-AUTH-TOKEN header", HttpStatus.UNAUTHORIZED);
        }

        // [STEP-02] JWT 토큰 검증
        if (!jwtTokenProvider.validateToken(authToken)) {
            log.warn("유효하지 않은 JWT 토큰");
            return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
        }

        try {
            Claims claims = jwtTokenProvider.getClaims(authToken);

            // [STEP-03] 토큰의 클레임 검증 및 사용자 정보 추출
            String email = claims.getSubject();
            String role = jwtTokenProvider.getUserRole(authToken);

            // ✅ JWT에서 userId 추출 (안전하게)
            Long userId = null;
            try {
                if (claims.get("userId") != null) {
                    userId = Long.valueOf(claims.get("userId").toString());
                } else if (claims.get("user_id") != null) {
                    userId = Long.valueOf(claims.get("user_id").toString());
                } else if (claims.get("id") != null) {
                    userId = Long.valueOf(claims.get("id").toString());
                }
            } catch (Exception e) {
                log.warn("userId 추출 실패 (계속 진행): {}", e.getMessage());
            }

            log.info("JWT 토큰 검증 성공: email={}, role={}, userId={}", email, role, userId);

            // ✅ 요청 헤더에 사용자 정보 추가
            ServerHttpRequest.Builder requestBuilder = request.mutate()
                    .header("X-USER-EMAIL", email)
                    .header("X-USER-ROLE", role);

            // ✅ userId가 있을 때만 헤더 추가
            if (userId != null) {
                requestBuilder.header("X-USER-ID", userId.toString());
                log.info("X-USER-ID 헤더 추가: {}", userId);
            } else {
                log.warn("JWT에서 userId를 찾을 수 없습니다");
            }

            ServerHttpRequest modifiedRequest = requestBuilder.build();
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("토큰 파싱 오류: {}", e.getMessage());
            return onError(exchange, "Token parsing error", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * ✅ 기존 방식 유지: 모든 서비스 경로를 PUBLIC으로 설정
     * (원래 잘 작동하던 방식)
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/auth/") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/fallback/") ||
                path.startsWith("/user-service") ||
                path.startsWith("/record-service") ||
                path.startsWith("/ormie-service") ||
                path.startsWith("/community-service") ||
                path.startsWith("/meeting-service") ||
                path.startsWith("/mountain-service") ||
                path.startsWith("/notification-service");


    }

    /**
     * 에러 응답 생성
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            String errorResponse = objectMapper.writeValueAsString(
                    Map.of(
                            "error", "Authentication Failed",
                            "message", message,
                            "status", status.value()
                    )
            );

            byte[] bytes = errorResponse.getBytes();
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (Exception e) {
            log.error("에러 응답 생성 실패", e);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1; // 높은 우선순위
    }
}