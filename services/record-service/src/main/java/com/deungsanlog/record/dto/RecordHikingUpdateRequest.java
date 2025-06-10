package com.deungsanlog.record.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordHikingUpdateRequest {
    private LocalDate recordDate;
    private String content;
    private MultipartFile photo; // 사진도 교체 가능하게
}
