package com.deungsanlog.notification.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignConfig {

    /**
     * Feign 요청 시 인증 헤더 전달
     * Gateway에서 받은 X-USER-* 헤더들을 다른 서비스로 전달
     */
    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // Gateway에서 전달받은 인증 정보 헤더들을 Feign 요청에 추가
                String userEmail = request.getHeader("X-USER-EMAIL");
                String userRole = request.getHeader("X-USER-ROLE");
                String userId = request.getHeader("X-USER-ID");
                String authToken = request.getHeader("X-AUTH-TOKEN");

                if (userEmail != null) {
                    requestTemplate.header("X-USER-EMAIL", userEmail);
                    log.debug("Feign 요청에 X-USER-EMAIL 헤더 추가: {}", userEmail);
                }

                if (userRole != null) {
                    requestTemplate.header("X-USER-ROLE", userRole);
                    log.debug("Feign 요청에 X-USER-ROLE 헤더 추가: {}", userRole);
                }

                if (userId != null) {
                    requestTemplate.header("X-USER-ID", userId);
                    log.debug("Feign 요청에 X-USER-ID 헤더 추가: {}", userId);
                }

                if (authToken != null) {
                    requestTemplate.header("X-AUTH-TOKEN", authToken);
                    log.debug("Feign 요청에 X-AUTH-TOKEN 헤더 추가");
                }
            }
        };
    }
}