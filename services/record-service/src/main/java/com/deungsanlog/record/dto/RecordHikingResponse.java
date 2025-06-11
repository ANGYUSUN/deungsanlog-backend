package com.deungsanlog.record.dto;

import com.deungsanlog.record.domain.RecordHiking;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordHikingResponse {
    private Long id;
    private Long userId;
    private Long mountainId;
    private String mountainName;
    private String photoUrl;
    private String content;
    private LocalDate recordDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RecordHikingResponse from(RecordHiking record) {
        return RecordHikingResponse.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .mountainId(record.getMountainId())
                .mountainName(record.getMountainName())
                .photoUrl(record.getPhotoUrl())
                .content(record.getContent())
                .recordDate(record.getRecordDate())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}

