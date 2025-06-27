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

    @GetMapping("/search")
    public ResponseEntity<?> searchMeetings(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "deadline") String sort,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(meetingService.searchMeetings(status, sort, keyword, page));
    }

    // 참가자만 조회
    @GetMapping("/{meetingId}/accepted-members")
    public ResponseEntity<?> getAcceptedMeetingMembers(@PathVariable Long meetingId) {
        return ResponseEntity.ok(meetingService.getAcceptedMeetingMembers(meetingId));
    }

    // 신청자만 조회
    @GetMapping("/{meetingId}/pending-applicants")
    public ResponseEntity<?> getPendingApplicants(@PathVariable Long meetingId) {
        return ResponseEntity.ok(meetingService.getPendingApplicants(meetingId));
    }

    // 모임 상세 조회
    @GetMapping("/{meetingId}")
    public ResponseEntity<?> getMeetingById(@PathVariable Long meetingId) {
        return ResponseEntity.ok(meetingService.getMeetingById(meetingId));
    }

    // 모임 신청
    @PostMapping("/{meetingId}/apply")
    public ResponseEntity<?> applyMeeting(
            @PathVariable Long meetingId,
            @RequestParam Long userId
    ) {
        meetingService.applyMeeting(meetingId, userId);
        return ResponseEntity.ok("신청 완료");
    }

    // 호스트가 신청자 수락
    @PatchMapping("/{meetingId}/members/{userId}/accept")
    public ResponseEntity<?> acceptMeetingMember(
            @PathVariable Long meetingId,
            @PathVariable Long userId
    ) {
        meetingService.acceptMeetingMember(meetingId, userId);
        return ResponseEntity.ok("수락 완료");
    }

    // 호스트가 신청자 거절
    @PatchMapping("/{meetingId}/members/{userId}/reject")
    public ResponseEntity<?> rejectMeetingMember(
            @PathVariable Long meetingId,
            @PathVariable Long userId
    ) {
        meetingService.rejectMeetingMember(meetingId, userId);
        return ResponseEntity.ok("거절 완료");
    }

    // 신청자가 본인의 신청을 취소
    @DeleteMapping("/{meetingId}/cancel")
    public ResponseEntity<?> cancelMeetingApplication(
            @PathVariable Long meetingId,
            @RequestParam Long userId
    ) {
        meetingService.cancelMeetingApplication(meetingId, userId);
        return ResponseEntity.ok("신청 취소 완료");
    }
}