package com.deungsanlog.mountain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 다중 사용자 알림 요청 DTO
 * Mountain Service → 산불/날씨 알림
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkNotificationRequest {
    private List<Long> userIds; // 받을 사용자 ID 목록
    private String type;        // "fire_risk", "weather_alert", etc.
    private String content;     // 알림 내용
    private String title;       // 알림 제목 (선택사항)
    private Long mountainId;    // 관련 산 ID (선택사항)
    private String mountainName; // 관련 산 이름 (선택사항)
}