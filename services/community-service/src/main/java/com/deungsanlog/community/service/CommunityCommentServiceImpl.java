package com.deungsanlog.community.service;

import com.deungsanlog.community.client.NotificationServiceClient;
import com.deungsanlog.community.client.UserClient;
import com.deungsanlog.community.domain.CommunityComment;
import com.deungsanlog.community.domain.CommunityPost;
import com.deungsanlog.community.dto.CommunityCommentRequest;
import com.deungsanlog.community.dto.CommunityCommentResponse;
import com.deungsanlog.community.dto.NotificationRequest;
import com.deungsanlog.community.repository.CommunityCommentRepository;
import com.deungsanlog.community.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityCommentServiceImpl implements CommunityCommentService {

    private final CommunityCommentRepository commentRepository;
    private final UserClient userClient;
    private final CommunityPostRepository postRepository;
    private final NotificationServiceClient notificationServiceClient;

    @Override
    @Transactional
    public CommunityComment writeComment(CommunityCommentRequest request) {
        CommunityPost post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        CommunityComment saved = commentRepository.save(
                CommunityComment.builder()
                        .postId(request.getPostId())
                        .userId(request.getUserId())
                        .content(request.getContent())
                        .parentCommentId(request.getParentCommentId())
                        .build()
        );
        postRepository.incrementCommentCount(request.getPostId());

        // ✅ 자기 댓글도 알림 전송 (조건 제거)
        sendCommentNotification(post, request.getUserId());

        return saved;
    }

    // 알림 전송 로직 - 자기 댓글도 알림 보내도록 수정
    private void sendCommentNotification(CommunityPost post, Long commenterId) {
        try {
            String commenterName = userClient.getNickname(commenterId);
            String title = "💬 새 댓글 알림";

            // ✅ 자기 댓글인 경우와 타인 댓글인 경우 구분
            String content;
            if (post.getUserId().equals(commenterId)) {
                // 자기 게시글에 자기가 댓글
                content = String.format("회원님이 '%s' 게시글에 댓글을 남겼습니다.",
                        post.getTitle().length() > 20
                                ? post.getTitle().substring(0, 20) + "..."
                                : post.getTitle());
            } else {
                // 타인이 댓글
                content = String.format("%s님이 '%s' 게시글에 댓글을 남겼습니다.",
                        commenterName,
                        post.getTitle().length() > 20
                                ? post.getTitle().substring(0, 20) + "..."
                                : post.getTitle());
            }

            // 디버깅: postId 확인
            log.info("🔍 댓글 알림 전송 전 postId 확인: postId={}, post.getId()={}", post.getId(), post.getId());

            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .userId(post.getUserId())  // 게시글 작성자에게 알림 (자기 자신 포함)
                    .type("comment")
                    .title(title)
                    .content(content)
                    .postId(post.getId())  // postId 추가
                    .build();

            // 디버깅: NotificationRequest 객체 확인
            log.info("🔍 NotificationRequest 객체 상세: userId={}, type={}, content={}, title={}, postId={}", 
                    notificationRequest.getUserId(), 
                    notificationRequest.getType(), 
                    notificationRequest.getContent(), 
                    notificationRequest.getTitle(), 
                    notificationRequest.getPostId());

            notificationServiceClient.sendNotification(notificationRequest);

            log.info("✅ 댓글 알림 전송 완료: postId={}, postAuthor={}, commenter={}",
                    post.getId(), post.getUserId(), commenterId);

        } catch (Exception e) {
            log.error("❌ 댓글 알림 전송 실패: postId={}, error={}", post.getId(), e.getMessage());
        }
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