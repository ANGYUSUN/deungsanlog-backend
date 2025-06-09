package com.deungsanlog.record.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordHikingCreateRequest {
    private Long userId;
    private Long mountainId;
    private LocalDate recordDate;
    private String content; // 100자 제한
    private MultipartFile photo;
}
