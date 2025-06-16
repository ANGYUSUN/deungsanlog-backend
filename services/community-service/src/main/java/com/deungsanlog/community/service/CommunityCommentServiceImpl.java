package com.deungsanlog.community.service;

import com.deungsanlog.community.client.UserClient;
import com.deungsanlog.community.domain.CommunityComment;
import com.deungsanlog.community.dto.CommunityCommentRequest;
import com.deungsanlog.community.dto.CommunityCommentResponse;
import com.deungsanlog.community.repository.CommunityCommentRepository;
import com.deungsanlog.community.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityCommentServiceImpl implements CommunityCommentService {
    private final CommunityCommentRepository commentRepository;
    private final UserClient userClient;
    private final CommunityPostRepository postRepository;

    @Override
    @Transactional
    public CommunityComment writeComment(CommunityCommentRequest request) {
        CommunityComment comment = CommunityComment.builder()
                .postId(request.getPostId())
                .userId(request.getUserId())
                .content(request.getContent())
                .parentCommentId(request.getParentCommentId())
                .build();
        // 댓글 저장
        CommunityComment saved = commentRepository.save(comment);

        // 댓글 수 증가
        postRepository.incrementCommentCount(request.getPostId());

        return saved;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        CommunityComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        // 댓글 삭제
        commentRepository.deleteById(commentId);

        // 댓글 수 감소
        postRepository.decrementCommentCount(comment.getPostId());
    }

    @Override
    public List<CommunityCommentResponse> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId).stream()
                .map(comment -> {
                    String nickname = userClient.getNickname(comment.getUserId());
                    return CommunityCommentResponse.from(comment, nickname);
                })
                .toList();
    }
}