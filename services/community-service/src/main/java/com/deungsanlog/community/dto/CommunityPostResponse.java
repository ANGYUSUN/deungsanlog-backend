package com.deungsanlog.community.dto;

import com.deungsanlog.community.domain.CommunityPost;
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

    public static CommunityPostResponse from(CommunityPost post, String nickname) {
        return CommunityPostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .nickname(nickname)
                .mountainId(post.getMountainId())
                .title(post.getTitle())
                .content(post.getContent())
                .hasImage(post.isHasImage())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt().toString())
                .updatedAt(post.getUpdatedAt().toString())
                .imageUrls(List.of()) // 구현 방식에 맞게 처리
                .build();
    }
}
