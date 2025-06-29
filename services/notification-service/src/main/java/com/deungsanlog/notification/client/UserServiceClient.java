package com.deungsanlog.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/internal/users/{userId}/fcm-token")
        // ✅ /api/users 추가
    String getFcmToken(@PathVariable("userId") Long userId);

    @PutMapping("/api/users/internal/users/{userId}/fcm-token")
        // ✅ /api/users 추가
    void updateFcmToken(@PathVariable("userId") Long userId, @RequestParam("token") String token);
}
