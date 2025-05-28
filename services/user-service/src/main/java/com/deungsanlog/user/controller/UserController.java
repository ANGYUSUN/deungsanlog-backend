package com.deungsanlog.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/status")
    public Map<String, String> getStatus() {
        return Map.of("message", "user-service is up!");
    }
}
