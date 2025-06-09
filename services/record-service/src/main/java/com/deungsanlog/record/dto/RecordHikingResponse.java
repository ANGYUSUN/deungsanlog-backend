package com.deungsanlog.record.dto;

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
    private String photoUrl;
    private String content;
    private LocalDate recordDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
