package com.deungsanlog.record.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    @GetMapping("/status")
    public Map<String, String> getStatus() {
        return Map.of("message", "record-service is up!");
    }
}
