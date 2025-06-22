package com.deungsanlog.meeting.controller;

import com.deungsanlog.meeting.dto.MeetingRequestDto;
import com.deungsanlog.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.ok().body("meeting-service is up!");
    }

    @PostMapping("/post")
    public ResponseEntity<?> createMeeting(@RequestBody MeetingRequestDto dto) {
        return ResponseEntity.ok(meetingService.saveMeeting(dto));
    }
}