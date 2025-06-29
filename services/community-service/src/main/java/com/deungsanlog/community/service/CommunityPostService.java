package com.deungsanlog.community.service;

import com.deungsanlog.community.dto.CommunityPostCreateRequest;
import com.deungsanlog.community.dto.CommunityPostResponse;
import com.deungsanlog.community.dto.CommunityPostUpdateRequest;

import java.util.List;
import java.util.Map;

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

    // totalPages 포함 검색 결과 반환
    Map<String, Object> searchPostsWithTotalPages(String sort, String field, String keyword, int page, int size);

    List<CommunityPostResponse> getPostsByUser(Long userId, int page, int size);

    // 전체 페이지 수와 게시글 목록을 반환하는 메서드 추가
    Map<String, Object> getPostsByUserWithTotalPages(Long userId, int page, int size);
}