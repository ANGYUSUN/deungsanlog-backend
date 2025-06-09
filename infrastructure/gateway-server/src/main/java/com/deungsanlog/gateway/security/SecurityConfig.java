package com.deungsanlog.gateway.security;


import com.deungsanlog.gateway.config.JwtAuthenticationManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationManager jwtAuthenticationManager;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        AuthenticationWebFilter jwtWebFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
        jwtWebFilter.setServerAuthenticationConverter(jwtAuthenticationConverter());

        return http
                // 인증 정보를 세션 등에 저장하지 않고, 매 요청마다 인증을 수행
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                // CSRF는 웹 브라우저 기반의 요청 위조 공격을 방지하기 위한 보안 메커니즘
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // HTTP 기본 인증(Basic Authentication)을 비활성화
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                // Spring Security의 기본 폼 로그인 기능 (HTML 로그인 페이지 제공)을 비활성화
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                // 경로별 인증 설정
                .authorizeExchange(exchanges -> exchanges
                        // 인증이 필요하지 않은 경로들 (GlobalGatewayFilter와 일치)
                        .pathMatchers("/auth/**").permitAll()                    // OAuth2 인증 관련
                        .pathMatchers("/actuator/**").permitAll()                // 헬스체크
                        .pathMatchers("/fallback/**").permitAll()                // Fallback 경로
                        .pathMatchers("/user-service/api/users/status").permitAll() // 상태 체크
                        .pathMatchers("/user-service/api/users/**").permitAll()
                        // 나머지 모든 HTTP 요청에 대해 인증된 사용자만 접근 가능하도록 설정
                        .anyExchange().authenticated()
                )

                // Spring Security의 기본 인증 필터 자리에 JWT 필터를 끼워 넣기
                .addFilterAt(jwtWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public ServerAuthenticationConverter jwtAuthenticationConverter() {
        return exchange -> {
            // X-AUTH-TOKEN 헤더에서 토큰 추출 ()
            String token = exchange.getRequest().getHeaders().getFirst("X-AUTH-TOKEN");
            if (token != null && !token.isEmpty()) {
                log.debug("X-AUTH-TOKEN 헤더에서 토큰 추출 성공");
                return Mono.just(new UsernamePasswordAuthenticationToken(null, token));
            }
            log.debug("X-AUTH-TOKEN 헤더가 없거나 비어있음");
            return Mono.empty();
        };
    }
}