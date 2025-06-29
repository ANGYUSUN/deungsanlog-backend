package com.deungsanlog.community.controller;

import com.deungsanlog.community.domain.CommunityComment;
import com.deungsanlog.community.dto.CommunityCommentRequest;
import com.deungsanlog.community.dto.CommunityCommentResponse;
import com.deungsanlog.community.service.CommunityCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communities/comments")
@RequiredArgsConstructor
public class CommunityCommentController {
    private final CommunityCommentService commentService;

    @PostMapping
    public CommunityComment writeComment(@RequestBody CommunityCommentRequest request) {
        return commentService.writeComment(request);
    }

    @GetMapping
    public List<CommunityCommentResponse> getComments(@RequestParam Long postId) {
        return commentService.getCommentsByPostId(postId);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
    }

}