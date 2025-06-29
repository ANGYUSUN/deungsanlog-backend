package com.deungsanlog.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostCreateRequest {
    private Long userId;
    private Long mountainId; // nullable
    private String title;
    private String content;
    private List<String> imageUrls; // 업로드된 이미지 URL 목록
}