package com.deungsanlog.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    //oauth 보호 기능에서 뺴야함.security 설정 필요.
    @GetMapping("/status")
    public Map<String, String> getStatus() {
        return Map.of("message", "user-service is up!");
    }


}
