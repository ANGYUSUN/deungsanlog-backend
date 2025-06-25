package com.deungsanlog.community.controller;

import com.deungsanlog.community.dto.CommunityPostCreateRequest;
import com.deungsanlog.community.dto.CommunityPostResponse;
import com.deungsanlog.community.dto.CommunityPostUpdateRequest;
import com.deungsanlog.community.repository.CommunityPostLikeRepository;
import com.deungsanlog.community.service.CommunityPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityPostService communityPostService;
    private final CommunityPostLikeRepository communityPostLikeRepository;

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("message", "community-service is up!");
    }

    @GetMapping("/posts")
    public ResponseEntity<List<CommunityPostResponse>> getAllPosts() {
        List<CommunityPostResponse> posts = communityPostService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    // 게시글 작성
    @PostMapping("/posts")
    public CommunityPostResponse createPost(@RequestBody CommunityPostCreateRequest request) {
        return communityPostService.createPost(request);
    }

    // 이미지 업로드
    @PostMapping("/posts/upload-image")
    public ResponseEntity<List<String>> uploadImages(@RequestParam("images") List<MultipartFile> files) {
        List<String> fileUrls = new ArrayList<>();
        String uploadDir = "C:/sw-project/deungsanlog-backend/services/community-service/uploads/";

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + filename);

                try {
                    Files.copy(file.getInputStream(), filePath);
                    fileUrls.add("/community-service/uploads/" + filename);
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }
        }

        return ResponseEntity.ok(fileUrls);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<CommunityPostResponse> getPostById(@PathVariable Long postId) {
        CommunityPostResponse post = communityPostService.getPostById(postId);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/posts/preview")
    public ResponseEntity<List<CommunityPostResponse>> getRecentPostsPreview() {
        List<CommunityPostResponse> recent10 = communityPostService.getRecentPosts(10);
        return ResponseEntity.ok(recent10);
    }

    // 게시글 수정
    @PutMapping("/posts/{postId}")
    public ResponseEntity<CommunityPostResponse> updatePost(
            @PathVariable Long postId,
            @RequestBody CommunityPostUpdateRequest request) {
        CommunityPostResponse updated = communityPostService.updatePost(postId, request);
        return ResponseEntity.ok(updated);
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        communityPostService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    // 게시글 좋아요
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long postId, @RequestParam Long userId) {
        communityPostService.likePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    // 게시글 좋아요 취소
    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId, @RequestParam Long userId) {
        communityPostService.unlikePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts/{postId}/like/status")
    public ResponseEntity<Boolean> isPostLiked(
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {
        boolean isLiked = communityPostLikeRepository.existsByPostIdAndUserId(postId, userId);
        return ResponseEntity.ok(isLiked);
    }

    // 검색/정렬/페이징 게시글 조회
    @GetMapping("/posts/search")
    public ResponseEntity<List<CommunityPostResponse>> searchPosts(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "all") String field,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<CommunityPostResponse> posts = communityPostService.searchPosts(sort, field, keyword, page, size);
        return ResponseEntity.ok(posts);
    }

    // 특정 사용자가 작성한 게시글 목록 조회
    @GetMapping("/posts/user/{userId}")
    public ResponseEntity<Map<String, Object>> getPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Map<String, Object> result = communityPostService.getPostsByUserWithTotalPages(userId, page, size);
        return ResponseEntity.ok(result);
    }
}