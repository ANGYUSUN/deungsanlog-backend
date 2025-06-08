package com.deungsanlog.gateway.config;


import com.deungsanlog.gateway.component.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        log.info("JWT 인증 시도 시작");

        // [STEP-01] 토큰 추출
        String token = authentication.getCredentials().toString();

        try {
            // [STEP-02] 서명검증 (멘토님의 JwtTokenProvider 사용)
            if (!jwtTokenProvider.validateToken(token)) {
                log.error("JWT 토큰 검증 실패");
                return Mono.error(new BadCredentialsException("Invalid token"));
            }

            // [STEP-03] 클레임 추출
            Claims claims = jwtTokenProvider.getClaims(token);
            String username = claims.getSubject(); // 이메일

            // 권한 정보 추출
            List<String> roles;

            // 멘토님 방식: "role" 클레임 먼저 확인
            String singleRole = jwtTokenProvider.getUserRole(token);
            if (singleRole != null) {
                roles = List.of(singleRole);
            } else {
                // "roles" 클레임 확인 (OAuth2에서 생성한 토큰용)
                roles = claims.get("roles", List.class);
                if (roles == null) {
                    roles = List.of("ROLE_USER"); // 기본 권한
                }
            }

            List<GrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            log.info("JWT 인증 성공: username={}, roles={}", username, roles);

            // [STEP-04] 인증 객체 생성
            return Mono.just(new UsernamePasswordAuthenticationToken(username, null, authorities));

        } catch (Exception e) {
            log.error("JWT 인증 실패: {}", e.getMessage());
            return Mono.error(new BadCredentialsException("Invalid token: " + e.getMessage()));
        }
    }
}