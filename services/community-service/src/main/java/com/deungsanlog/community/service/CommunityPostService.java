package com.deungsanlog.community.service;

import com.deungsanlog.community.dto.CommunityPostCreateRequest;
import com.deungsanlog.community.dto.CommunityPostResponse;

import java.util.List;

public interface CommunityPostService {
    CommunityPostResponse createPost(CommunityPostCreateRequest request);

    List<CommunityPostResponse> getAllPosts();
}
