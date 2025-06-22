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
        return ResponseEntity.ok("meeting-service is up!");
    }

    @PostMapping("/post")
    public ResponseEntity<?> createMeeting(@RequestBody MeetingRequestDto dto) {
        return ResponseEntity.ok(meetingService.saveMeeting(dto));
    }

    // 전체 모임 목록 조회
    @GetMapping("/all")
    public ResponseEntity<?> getAllMeetings(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(meetingService.getAllMeetings(page));
    }


    // 특정 모임의 멤버 목록 조회
    @GetMapping("/{meetingId}/members")
    public ResponseEntity<?> getMeetingMembers(@PathVariable Long meetingId) {
        return ResponseEntity.ok(meetingService.getMeetingMembers(meetingId));
    }
}