package com.deungsanlog.community.dto;

import lombok.Getter;

@Getter
public class CommunityCommentRequest {
    private Long postId;
    private Long userId;
    private String content;
    private Long parentCommentId; // 대댓글일 경우
}