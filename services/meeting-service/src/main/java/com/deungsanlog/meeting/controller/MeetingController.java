package com.deungsanlog.meeting.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {


    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("message", "meeting-service is up!");
    }
}