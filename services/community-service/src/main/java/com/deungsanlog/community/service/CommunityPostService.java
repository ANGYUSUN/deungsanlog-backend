package com.deungsanlog.community.service;

import com.deungsanlog.community.dto.CommunityPostCreateRequest;
import com.deungsanlog.community.dto.CommunityPostResponse;
import com.deungsanlog.community.dto.CommunityPostUpdateRequest;

import java.util.List;

public interface CommunityPostService {
    CommunityPostResponse createPost(CommunityPostCreateRequest request);

    List<CommunityPostResponse> getAllPosts();

    CommunityPostResponse getPostById(Long postId);

    List<CommunityPostResponse> getRecentPosts(int limit);

    CommunityPostResponse updatePost(Long postId, CommunityPostUpdateRequest request);

    void deletePost(Long postId);

    void likePost(Long postId, Long userId);

    void unlikePost(Long postId, Long userId);

    List<CommunityPostResponse> searchPosts(String sort, String field, String keyword, int page, int size);

    List<CommunityPostResponse> getPostsByUser(Long userId, int page, int size);
}