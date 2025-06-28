package com.deungsanlog.notification.controller;

import com.deungsanlog.notification.dto.BulkNotificationRequest;
import com.deungsanlog.notification.dto.NotificationRequest;
import com.deungsanlog.notification.entity.Notification;
import com.deungsanlog.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ========== 📋 서비스 상태 체크 ==========

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        return ResponseEntity.ok(Map.of("message", "notification-service is up!"));
    }

    // ========== 📨 다른 서비스에서 호출하는 알림 전송 API ==========

    /**
     * 단일 사용자 알림 전송 (Community Service → 댓글/좋아요)
     * POST /api/notifications/send
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest request) {
        log.info("📨 외부 서비스 알림 요청: userId={}, type={}", request.getUserId(), request.getType());

        try {
            notificationService.sendNotificationToUser(
                    request.getUserId(),
                    request.getType(),
                    request.getContent()
            );

            return ResponseEntity.ok(Map.of("message", "알림 전송 완료"));
        } catch (Exception e) {
            log.error("❌ 외부 알림 전송 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "알림 전송 실패"));
        }
    }

    /**
     * 다중 사용자 알림 전송 (Mountain Service → 산불/날씨)
     * POST /api/notifications/bulk-send
     */
    @PostMapping("/bulk-send")
    public ResponseEntity<?> sendBulkNotification(@RequestBody BulkNotificationRequest request) {
        log.info("📨 외부 서비스 대량 알림: userIds={}, type={}", request.getUserIds().size(), request.getType());

        try {
            // 산 정보가 있으면 알림 내용에 포함
            String content = request.getContent();
            if (request.getMountainName() != null) {
                content = String.format("[%s] %s", request.getMountainName(), content);
            }

            notificationService.sendNotificationToUsers(
                    request.getUserIds(),
                    request.getType(),
                    content
            );

            return ResponseEntity.ok(Map.of(
                    "message", "대량 알림 전송 완료",
                    "sentCount", request.getUserIds().size()
            ));
        } catch (Exception e) {
            log.error("❌ 외부 대량 알림 전송 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "대량 알림 전송 실패"));
        }
    }

    // ========== 📱 사용자용 API (프론트엔드에서 호출) ==========

    /**
     * FCM 토큰 저장
     * POST /api/notifications/fcm-token
     */
    @PostMapping("/fcm-token")
    public ResponseEntity<?> saveFcmToken(
            @RequestHeader("X-AUTH-TOKEN") String authToken,
            @RequestBody Map<String, String> requestBody) {

        log.info("🔑 FCM 토큰 저장 요청 받음");

        Long userId = extractUserIdFromJWT(authToken);
        log.info("🔍 추출된 userId: {}", userId);

        if (userId == null) {
            log.error("❌ JWT에서 userId 추출 실패");
            return ResponseEntity.badRequest().body(Map.of("error", "JWT 토큰이 유효하지 않습니다"));
        }

        String token = requestBody.get("token");
        if (token == null || token.isBlank()) {
            log.error("❌ FCM 토큰이 비어있음");
            return ResponseEntity.badRequest().body(Map.of("error", "토큰이 비어 있습니다"));
        }

        try {
            notificationService.saveFcmToken(userId, token);
            log.info("✅ FCM 토큰 저장 성공: userId={}", userId);
            return ResponseEntity.ok(Map.of("message", "FCM 토큰이 저장되었습니다"));
        } catch (Exception e) {
            log.error("❌ FCM 토큰 저장 실패: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "토큰 저장 실패"));
        }
    }

    /**
     * 사용자 알림 목록 조회
     * GET /api/notifications
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUserNotifications(
            @RequestHeader("X-AUTH-TOKEN") String authToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = extractUserIdFromJWT(authToken);
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "JWT 토큰이 유효하지 않습니다"));
        }

        try {
            Page<Notification> notifications = notificationService.getNotificationsByUserId(userId, PageRequest.of(page, size));
            long unreadCount = notificationService.getUnreadCount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("content", notifications.getContent());
            response.put("currentPage", notifications.getNumber());
            response.put("totalItems", notifications.getTotalElements());
            response.put("totalPages", notifications.getTotalPages());
            response.put("unreadCount", unreadCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 알림 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "알림 조회 실패"));
        }
    }

    /**
     * 읽지 않은 알림 개수만 조회
     * GET /api/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@RequestHeader("X-AUTH-TOKEN") String authToken) {

        Long userId = extractUserIdFromJWT(authToken);
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "JWT 토큰이 유효하지 않습니다"));
        }

        try {
            long unreadCount = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "조회 실패"));
        }
    }

    /**
     * 알림 읽음 처리
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(
            @RequestHeader("X-AUTH-TOKEN") String authToken,
            @PathVariable Long id) {

        Long userId = extractUserIdFromJWT(authToken);
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "JWT 토큰이 유효하지 않습니다"));
        }

        try {
            notificationService.markAsRead(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 알림 삭제
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @RequestHeader("X-AUTH-TOKEN") String authToken,
            @PathVariable Long id) {

        Long userId = extractUserIdFromJWT(authToken);
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "JWT 토큰이 유효하지 않습니다"));
        }

        try {
            notificationService.deleteNotification(id, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== 🔧 JWT 파싱 유틸리티 ==========

    /**
     * ✅ 개선된 JWT 파싱 메서드 (정규식 사용)
     */
    private Long extractUserIdFromJWT(String authToken) {
        try {
            if (authToken == null || authToken.isBlank()) {
                log.warn("⚠️ JWT 토큰이 비어있음");
                return null;
            }

            String[] chunks = authToken.split("\\.");
            if (chunks.length != 3) {
                log.warn("⚠️ JWT 형식이 올바르지 않음");
                return null;
            }

            String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
            log.debug("🔍 JWT payload: {}", payload);

            // ✅ 정규식을 사용한 안전한 userId 추출
            Pattern pattern = Pattern.compile("\"userId\"\\s*:\\s*(\\d+)");
            Matcher matcher = pattern.matcher(payload);

            if (matcher.find()) {
                Long userId = Long.parseLong(matcher.group(1));
                log.debug("✅ userId 추출 성공: {}", userId);
                return userId;
            }

            log.warn("⚠️ JWT에서 userId를 찾을 수 없음");
            return null;

        } catch (Exception e) {
            log.error("❌ JWT 파싱 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 🔍 JWT 디버깅용 임시 API (개발/테스트용)
     */
    @GetMapping("/debug/jwt")
    public ResponseEntity<?> debugJwt(@RequestHeader("X-AUTH-TOKEN") String authToken) {
        try {
            log.info("🔍 JWT 디버깅 요청");

            String[] chunks = authToken.split("\\.");
            if (chunks.length != 3) {
                return ResponseEntity.badRequest().body(Map.of("error", "잘못된 JWT 형식"));
            }

            // Header 디코딩
            String header = new String(java.util.Base64.getUrlDecoder().decode(chunks[0]));

            // Payload 디코딩
            String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));

            log.info("🔍 JWT Header: {}", header);
            log.info("🔍 JWT Payload: {}", payload);

            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("header", header);
            debugInfo.put("payload", payload);
            debugInfo.put("extractedUserId", extractUserIdFromJWT(authToken));

            return ResponseEntity.ok(debugInfo);

        } catch (Exception e) {
            log.error("❌ JWT 디버깅 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}