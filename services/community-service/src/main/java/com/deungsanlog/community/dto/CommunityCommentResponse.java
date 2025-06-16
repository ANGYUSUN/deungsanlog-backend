package com.deungsanlog.community.dto;

import com.deungsanlog.community.domain.CommunityComment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommunityCommentResponse {
    private Long id;
    private Long userId;
    private String content;
    private String nickname; // 닉네임 추가
    private LocalDateTime createdAt;

    public static CommunityCommentResponse from(CommunityComment comment, String nickname) {
        return new CommunityCommentResponse(
                comment.getId(),
                comment.getUserId(),
                comment.getContent(),
                nickname,
                comment.getCreatedAt()
        );
    }
}
