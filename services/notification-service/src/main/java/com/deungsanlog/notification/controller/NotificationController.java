package com.deungsanlog.notification.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @GetMapping("/status")
    public Map<String, String> getStatus() {
        return Map.of("message", "notification-service is up!");
    }
}
