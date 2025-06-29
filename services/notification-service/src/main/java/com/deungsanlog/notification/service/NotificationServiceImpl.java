package com.deungsanlog.notification.service;

import com.deungsanlog.notification.client.UserServiceClient;
import com.deungsanlog.notification.entity.Notification;
import com.deungsanlog.notification.repository.NotificationRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final UserServiceClient userServiceClient;
    private final NotificationRepository notificationRepository;

    // ========== 📱 FCM 알림 전송 기능 ==========

    @Transactional
    @Override
    public void sendNotificationToUser(Long userId, String type, String content) {
        log.info("📨 단일 사용자 알림 전송: userId={}, type={}", userId, type);

        try {
            // 1. FCM 토큰 조회
            String fcmToken = userServiceClient.getFcmToken(userId);

            // 2. FCM 푸시 알림 전송 (토큰이 있는 경우에만)
            if (fcmToken != null && !fcmToken.isBlank()) {
                sendFcmMessage(fcmToken, getNotificationTitle(type), content);
                log.info("📨 FCM 푸시 전송 완료: userId={}", userId);
            } else {
                log.warn("⚠️ FCM 토큰 없음 - DB 알림만 저장: userId={}", userId);
            }

            // 3. DB에 알림 저장 (FCM 성공/실패와 무관하게 항상 저장)
            saveNotificationToDb(userId, type, content);
            log.info("💾 DB 알림 저장 완료: userId={}", userId);

        } catch (Exception e) {
            log.error("❌ 알림 전송 실패: userId={}, error={}", userId, e.getMessage());

            // FCM 실패해도 DB 알림은 저장 시도
            try {
                saveNotificationToDb(userId, type, content);
                log.info("💾 FCM 실패 후 DB 알림만 저장: userId={}", userId);
            } catch (Exception dbError) {
                log.error("❌ DB 알림 저장도 실패: userId={}", userId);
                throw new RuntimeException("알림 저장 완전 실패", dbError);
            }
        }
    }

    @Transactional
    @Override
    public void sendNotificationToUser(Long userId, String type, String content, Long meetingId) {
        log.info("📨 단일 사용자 알림 전송 (모임 ID 포함): userId={}, type={}, meetingId={}", userId, type, meetingId);

        try {
            // 1. FCM 토큰 조회
            String fcmToken = userServiceClient.getFcmToken(userId);

            // 2. FCM 푸시 알림 전송 (토큰이 있는 경우에만)
            if (fcmToken != null && !fcmToken.isBlank()) {
                sendFcmMessage(fcmToken, getNotificationTitle(type), content);
                log.info("📨 FCM 푸시 전송 완료: userId={}", userId);
            } else {
                log.warn("⚠️ FCM 토큰 없음 - DB 알림만 저장: userId={}", userId);
            }

            // 3. DB에 알림 저장 (모임 ID 포함)
            saveNotificationToDb(userId, type, content, meetingId);
            log.info("💾 DB 알림 저장 완료 (모임 ID 포함): userId={}, meetingId={}", userId, meetingId);

        } catch (Exception e) {
            log.error("❌ 알림 전송 실패: userId={}, error={}", userId, e.getMessage());

            // FCM 실패해도 DB 알림은 저장 시도
            try {
                saveNotificationToDb(userId, type, content, meetingId);
                log.info("💾 FCM 실패 후 DB 알림만 저장 (모임 ID 포함): userId={}, meetingId={}", userId, meetingId);
            } catch (Exception dbError) {
                log.error("❌ DB 알림 저장도 실패: userId={}", userId);
                throw new RuntimeException("알림 저장 완전 실패", dbError);
            }
        }
    }

    @Transactional
    @Override
    public void sendNotificationToUsers(List<Long> userIds, String type, String content) {
        log.info("📨 다중 사용자 알림 전송: 대상 {}명, type={}", userIds.size(), type);

        int successCount = 0;
        for (Long userId : userIds) {
            try {
                sendNotificationToUser(userId, type, content);
                successCount++;
            } catch (Exception e) {
                log.error("❌ 개별 사용자 알림 실패: userId={}", userId, e);
                // 개별 실패가 전체를 막지 않도록 continue
            }
        }

        log.info("✅ 다중 알림 전송 완료: 성공 {}/{}", successCount, userIds.size());
    }

    @Override
    public void saveFcmToken(Long userId, String fcmToken) {
        log.info("🔑 FCM 토큰 저장: userId={}", userId);

        try {
            userServiceClient.updateFcmToken(userId, fcmToken);
            log.info("✅ FCM 토큰 저장 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("❌ FCM 토큰 저장 실패: userId={}", userId, e);
            throw new RuntimeException("FCM 토큰 저장 실패", e);
        }
    }

    // ========== 📋 DB 알림 관리 기능 ==========

    @Override
    public Page<Notification> getNotificationsByUserId(Long userId, Pageable pageable) {
        log.debug("📋 알림 목록 조회: userId={}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional
    @Override
    public void markAsRead(Long notificationId, Long userId) {
        log.info("✅ 알림 읽음 처리: notificationId={}, userId={}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("알림이 존재하지 않습니다: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new SecurityException("다른 사용자의 알림은 읽을 수 없습니다");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
            log.info("✅ 알림 읽음 처리 완료: notificationId={}", notificationId);
        }
    }

    @Transactional
    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        log.info("🗑️ 알림 삭제: notificationId={}, userId={}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("알림이 존재하지 않습니다: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new SecurityException("다른 사용자의 알림은 삭제할 수 없습니다");
        }

        notificationRepository.delete(notification);
        log.info("✅ 알림 삭제 완료: notificationId={}", notificationId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    @Override
    public void markAllAsRead(Long userId) {
        log.info("✅ 모든 알림 읽음 처리: userId={}", userId);

        try {
            int updatedCount = notificationRepository.markAllAsReadByUserId(userId);
            log.info("✅ 모든 알림 읽음 처리 완료: userId={}, updatedCount={}", userId, updatedCount);
        } catch (Exception e) {
            log.error("❌ 모든 알림 읽음 처리 실패: userId={}", userId, e);
            throw new RuntimeException("모든 알림 읽음 처리 실패", e);
        }
    }

    // ========== 🔧 Private 헬퍼 메서드들 ==========

    private void sendFcmMessage(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setWebpushConfig(WebpushConfig.builder()
                            .setNotification(new WebpushNotification(title, body))
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.debug("📨 FCM 전송 성공: response={}", response);

        } catch (Exception e) {
            log.error("❌ FCM 메시지 전송 실패: {}", e.getMessage());
            throw new RuntimeException("FCM 전송 실패", e);
        }
    }

    private void saveNotificationToDb(Long userId, String type, String content) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .content(content)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    private void saveNotificationToDb(Long userId, String type, String content, Long meetingId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .content(content)
                .meetingId(meetingId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    private String getNotificationTitle(String type) {
        return switch (type) {
            case "comment" -> "💬 새 댓글 알림";
            case "like" -> "❤️ 좋아요 알림";
            case "fire_risk" -> "🔥 산불 위험 알림";
            case "weather_alert" -> "🌧️ 날씨 경보";
            case "meeting_apply" -> "👥 모임 참가신청";
            case "meeting_accepted" -> "✅ 모임 참가 수락";
            case "meeting_full" -> "🎯 모임 정원 마감";
            case "meeting_closed" -> "🔒 모임 마감";
            case "system" -> "⚙️ 시스템 알림";
            default -> "📱 등산로그 알림";
        };
    }
}