package com.deungsanlog.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("prod") // 프로필 명시
public class WebMvcProdConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/communities/uploads/**")
                .addResourceLocations("file:/home/rocky/backend-deploy/community-service/uploads/");
    }
}
