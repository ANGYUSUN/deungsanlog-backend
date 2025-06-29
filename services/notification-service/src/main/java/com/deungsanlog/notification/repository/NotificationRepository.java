package com.deungsanlog.notification.repository;

import com.deungsanlog.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 사용자별 알림 목록 조회 (생성일 최신순, 페이징)
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자별 알림 목록 조회 (생성일 최신순, 리스트)
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자별 읽지 않은 알림 개수
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * 사용자별 전체 알림 개수
     */
    long countByUserId(Long userId);

    /**
     * 특정 사용자의 특정 알림 조회 (보안용)
     */
    @Query("SELECT n FROM Notification n WHERE n.id = :notificationId AND n.userId = :userId")
    Notification findByIdAndUserId(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    /**
     * 사용자의 읽지 않은 알림 목록 조회
     */
    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 특정 타입의 알림 조회
     */
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type, Pageable pageable);

    /**
     * 사용자의 모든 알림 삭제 (회원 탈퇴시 사용)
     */
    void deleteByUserId(Long userId);

    /**
     * 사용자의 모든 읽지 않은 알림을 읽음으로 표시
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);
}