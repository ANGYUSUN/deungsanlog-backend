package com.deungsanlog.community.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CommunityPostUpdateRequest {
    private String title;
    private String content;
    private Long mountainId;
    private List<String> imageUrls; // 이미지 URL 목록
}