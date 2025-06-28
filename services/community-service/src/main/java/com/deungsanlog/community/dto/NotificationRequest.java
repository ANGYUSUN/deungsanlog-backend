package com.deungsanlog.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Long userId;        // 받을 사용자 ID
    private String type;        // "comment", "like"
    private String content;     // 알림 내용
    private String title;       // 알림 제목 (선택사항)
}