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
public class CommunityPostResponse {
    private Long id;
    private Long userId;
    private String nickname;
    private Long mountainId;
    private String title;
    private String content;
    private boolean hasImage;
    private int likeCount;
    private int commentCount;
    private String createdAt;
    private String updatedAt;
    private List<String> imageUrls;
}
