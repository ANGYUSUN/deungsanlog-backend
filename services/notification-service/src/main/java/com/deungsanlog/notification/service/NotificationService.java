package com.deungsanlog.notification.service;

import com.deungsanlog.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 📱 통합 알림 서비스 (FCM + DB 알림)
 */
public interface NotificationService {

    // ========== 📱 FCM 알림 전송 기능 ==========

    /**
     * 단일 사용자에게 알림 전송 (FCM + DB 저장)
     *
     * @param userId  사용자 ID
     * @param type    알림 타입 ("comment", "like", "fire_risk", "weather_alert")
     * @param content 알림 내용
     */
    void sendNotificationToUser(Long userId, String type, String content);

    /**
     * 단일 사용자에게 알림 전송 (모임 ID 포함)
     *
     * @param userId  사용자 ID
     * @param type    알림 타입
     * @param content 알림 내용
     * @param meetingId 모임 ID
     */
    void sendNotificationToUser(Long userId, String type, String content, Long meetingId);

    /**
     * 여러 사용자에게 알림 전송 (FCM + DB 저장)
     *
     * @param userIds 사용자 ID 목록
     * @param type    알림 타입
     * @param content 알림 내용
     */
    void sendNotificationToUsers(List<Long> userIds, String type, String content);

    /**
     * FCM 토큰 저장/업데이트
     *
     * @param userId   사용자 ID
     * @param fcmToken FCM 토큰
     */
    void saveFcmToken(Long userId, String fcmToken);

    // ========== 📋 DB 알림 관리 기능 ==========

    /**
     * 사용자별 알림 목록 조회 (페이징)
     */
    Page<Notification> getNotificationsByUserId(Long userId, Pageable pageable);

    /**
     * 알림을 읽음으로 표시
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 알림 삭제
     */
    void deleteNotification(Long notificationId, Long userId);

    /**
     * 사용자의 읽지 않은 알림 개수 조회
     */
    long getUnreadCount(Long userId);

    /**
     * 사용자의 모든 알림을 읽음으로 표시
     */
    void markAllAsRead(Long userId);
}