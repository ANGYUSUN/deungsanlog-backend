package com.deungsanlog.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/**").permitAll()           // 인증 경로 허용
                        .pathMatchers("/actuator/**").permitAll()       // 헬스체크 허용
                        .pathMatchers("/oauth2/**").permitAll()         // OAuth2 엔드포인트 허용
                        .pathMatchers("/fallback/**").permitAll()       // Fallback 경로 허용
                        .anyExchange().authenticated()                  // 나머지는 인증 필요
                )
                .oauth2Login(oauth2 -> {
                    // OAuth2 로그인 설정 (필요시 추가 설정)
                })
                .build();
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        System.out.println(" JWT 토큰 프로바이더 Bean 생성 중...");
        return new JwtTokenProvider();
    }
}