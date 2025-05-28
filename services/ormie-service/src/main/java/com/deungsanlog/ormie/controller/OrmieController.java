package com.deungsanlog.ormie.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ormie")
public class OrmieController {

    @GetMapping("/status")
    public Map<String, String> getStatus() {
        return Map.of("message", "ormie-service is up!");
    }
}
