package com.deungsanlog.mountain.client;

import com.deungsanlog.mountain.dto.BulkNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {
    @PostMapping("/api/notifications/bulk-send")
    ResponseEntity<?> sendBulkNotification(@RequestBody BulkNotificationRequest request);
}