package com.deungsanlog.mountain.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/mountains")
public class MountainController {

    @GetMapping("/status")
    public Map<String, String> status(){
        return Map.of("message", "mountain-service is up!");
    }
}