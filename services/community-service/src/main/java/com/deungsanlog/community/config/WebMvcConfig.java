package com.deungsanlog.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/communities/uploads/**")
                .addResourceLocations(
                        "file:/home/rocky/backend-deploy/community-service/uploads/",
                        "file:C:/sw-project/deungsanlog-backend/services/community-service/uploads/"
                );
    }
}
