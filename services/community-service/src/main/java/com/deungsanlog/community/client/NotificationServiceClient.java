package com.deungsanlog.community.client;

import com.deungsanlog.community.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {
    @PostMapping("/api/notifications/send")
    ResponseEntity<?> sendNotification(@RequestBody NotificationRequest request);
}