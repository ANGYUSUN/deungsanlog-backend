package com.deungsanlog.community.service;

import com.deungsanlog.community.domain.CommunityComment;
import com.deungsanlog.community.dto.CommunityCommentRequest;
import com.deungsanlog.community.dto.CommunityCommentResponse;

import java.util.List;


public interface CommunityCommentService {
    CommunityComment writeComment(CommunityCommentRequest request);

    List<CommunityCommentResponse> getCommentsByPostId(Long postId);

    void deleteComment(Long commentId);
}