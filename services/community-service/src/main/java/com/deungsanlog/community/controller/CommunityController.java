package com.deungsanlog.community.controller;

import com.deungsanlog.community.dto.CommunityPostCreateRequest;
import com.deungsanlog.community.dto.CommunityPostResponse;
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

    // 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        communityPostService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
}