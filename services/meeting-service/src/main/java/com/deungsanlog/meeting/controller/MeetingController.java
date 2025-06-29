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
    public ResponseEntity<?> getAllMeetings(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        var meetingPage = meetingService.getAllMeetings(page, size);
        return ResponseEntity.ok(
                java.util.Map.of(
                        "meetings", meetingPage.getContent(),
                        "size", meetingPage.getSize(),
                        "totalPages", meetingPage.getTotalPages()
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchMeetings(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "deadline") String sort,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var meetingPage = meetingService.searchMeetings(status, sort, keyword, page, size);
        return ResponseEntity.ok(
                java.util.Map.of(
                        "meetings", meetingPage.getContent(),
                        "size", meetingPage.getSize(),
                        "totalPages", meetingPage.getTotalPages()
                )
        );
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

    // 모임 CLOSE 처리
    @PatchMapping("/{meetingId}/closed")
    public ResponseEntity<?> closeMeeting(@PathVariable Long meetingId) {
        meetingService.closeMeeting(meetingId);
        return ResponseEntity.ok("모임이 CLOSED 상태로 변경되었습니다.");
    }

    // 모임 CANCELLED 처리
    @PatchMapping("/{meetingId}/cancelled")
    public ResponseEntity<?> cancelMeeting(@PathVariable Long meetingId) {
        meetingService.cancelMeeting(meetingId);
        return ResponseEntity.ok("모임이 CANCELLED 상태로 변경되었습니다.");
    }

    // userId가 시도했던 모든 모임(meetingId) 리스트 반환 (상태 무관)
    @GetMapping("/my-all-meeting-ids")
    public ResponseEntity<?> getAllMyMeetingIds(@RequestParam Long userId) {
        return ResponseEntity.ok(meetingService.getAllMeetingIdsByUserId(userId));
    }

    // userId가 ACCEPTED 상태로 참여중인 meetingId 리스트 반환
    @GetMapping("/my-meeting-ids")
    public ResponseEntity<?> getMyMeetingIds(@RequestParam Long userId) {
        return ResponseEntity.ok(meetingService.getAcceptedMeetingIdsByUserId(userId));
    }

    // userId가 개설한 meetingId 리스트 반환
    @GetMapping("/my-hosted-meeting-ids")
    public ResponseEntity<?> getMyHostedMeetingIds(@RequestParam Long userId) {
        return ResponseEntity.ok(meetingService.getHostedMeetingIdsByUserId(userId));
    }

    // userId가 참여한 모임을 상태별로 필터링
    @GetMapping("/my-meeting-ids-by-status")
    public ResponseEntity<?> getMyMeetingIdsByStatus(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "all") String status
    ) {
        return ResponseEntity.ok(meetingService.getMeetingIdsByUserIdAndStatus(userId, status));
    }

    // userId가 개설한 모임을 상태별로 필터링
    @GetMapping("/my-hosted-meeting-ids-by-status")
    public ResponseEntity<?> getMyHostedMeetingIdsByStatus(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "all") String status
    ) {
        return ResponseEntity.ok(meetingService.getHostedMeetingIdsByUserIdAndStatus(userId, status));
    }

    // 내 모임 조회 (상태, 정렬, 날짜 필터링 포함)
    @GetMapping("/my-meetings-filtered")
    public ResponseEntity<?> getMyMeetingsFiltered(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "participated") String type, // participated 또는 hosted
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "latest") String sort, // latest, oldest, deadline
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var result = meetingService.getMyMeetingsFiltered(userId, type, status, sort, startDate, endDate, page, size);
        return ResponseEntity.ok(result);
    }
}