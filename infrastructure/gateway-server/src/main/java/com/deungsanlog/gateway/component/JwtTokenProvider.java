package com.deungsanlog.gateway.component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24시간 기본값
    private long tokenValidityInMilliseconds;

    // 멘토님 원본 메서드들
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserId(String token) {
        return getClaims(token).getSubject(); // "sub" claim
    }

    public String getUserRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // ========== OAuth2용 추가 메서드 ==========

    /**
     * OAuth2 로그인용 JWT 토큰 생성
     */
    public String generateToken(String email, List<String> roles, Long userId) {
        log.info("JWT 토큰 생성: email={}, userId={}", email, userId);

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", roles.get(0)); // 사용하는 "role" 클레임
        claims.put("roles", roles); // 추가 정보용
        claims.put("userId", userId);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    /**
     * 토큰에서 사용자 이메일 추출
     */
    public String getUserEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 토큰에서 사용자 ID 추출 (Long 타입)
     */
    public Long getUserIdAsLong(String token) {
        return getClaims(token).get("userId", Long.class);
    }
}