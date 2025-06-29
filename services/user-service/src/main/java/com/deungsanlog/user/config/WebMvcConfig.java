package com.deungsanlog.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 프로필 이미지 정적 파일 서빙 (실제 폴더 구조에 맞춤)
        registry.addResourceHandler("/uploads/profiles/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/services/user-service/uploads/profiles/");

        // 로그 출력 (디버깅용)
        System.out.println("📁 프로필 이미지 경로 설정:");
        System.out.println("   URL 패턴: /uploads/profiles/**");
        System.out.println("   실제 경로: " + System.getProperty("user.dir") + "/uploads/profiles/");
    }
}