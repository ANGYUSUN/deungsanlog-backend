package com.deungsanlog.record.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordHikingCreateRequest {
    private Long userId;
    private Long mountainId;
    private String mountainName; // 산 이름
    private LocalDate recordDate;
    private String content; // 100자 제한
    private MultipartFile photo;
}
