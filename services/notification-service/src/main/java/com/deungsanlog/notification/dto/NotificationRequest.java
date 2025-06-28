package com.deungsanlog.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 단일 사용자 알림 요청 DTO
 * Community Service → 댓글/좋아요 알림
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Long userId;        // 받을 사용자 ID
    private String type;        // "comment", "like", "system", etc.
    private String content;     // 알림 내용
    private String title;       // 알림 제목 (선택사항)
}