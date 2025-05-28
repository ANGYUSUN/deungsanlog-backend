package com.deungsanlog.community.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("message", "community-service is up!");
    }

}