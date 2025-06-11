package com.deungsanlog.record.controller;

import com.deungsanlog.record.dto.RecordHikingResponse;
import com.deungsanlog.record.service.RecordHikingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordHikingService recordHikingService;

    @GetMapping("/status")
    public Map<String, String> getStatus() {
        return Map.of("message", "record-service is up!");
    }

    @GetMapping("/get")
    public ResponseEntity<List<RecordHikingResponse>> getRecordsByUser(
            @RequestParam("userId") Long userId
    ) {
        return ResponseEntity.ok(recordHikingService.getRecordsByUser(userId));
    }

    @PostMapping("/post")
    public ResponseEntity<String> createRecord(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "mountainId", required = false) Long mountainId,
            @RequestParam("mountainName") String mountainName,
            @RequestParam("recordDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordDate,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam("photo") MultipartFile photo
    ) {
        recordHikingService.create(userId, mountainId, mountainName, recordDate, content, photo);
        return ResponseEntity.ok("등산 기록이 성공적으로 저장되었습니다!");
    }


    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteRecord(@RequestParam("recordId") Long recordId) {
        recordHikingService.delete(recordId);
        return ResponseEntity.ok("등산 기록이 성공적으로 삭제되었습니다!");
    }
}