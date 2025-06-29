package com.deungsanlog.community.service;

import com.deungsanlog.community.client.NotificationServiceClient;
import com.deungsanlog.community.client.UserClient;
import com.deungsanlog.community.domain.CommunityPost;
import com.deungsanlog.community.domain.CommunityPostImage;
import com.deungsanlog.community.domain.CommunityPostLike;
import com.deungsanlog.community.dto.CommunityPostCreateRequest;
import com.deungsanlog.community.dto.CommunityPostResponse;
import com.deungsanlog.community.dto.CommunityPostUpdateRequest;
import com.deungsanlog.community.dto.NotificationRequest;
import com.deungsanlog.community.repository.CommunityPostImageRepository;
import com.deungsanlog.community.repository.CommunityPostLikeRepository;
import com.deungsanlog.community.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityPostServiceImpl implements CommunityPostService {

    private final CommunityPostRepository postRepository;
    private final CommunityPostImageRepository imageRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserClient userClient;
    private final CommunityPostLikeRepository communityPostLikeRepository;

    // ❤️ 알림 서비스 추가
    private final NotificationServiceClient notificationServiceClient;

    @Value("${community.upload-path}")
    private String uploadDir;

    @Override
    @Transactional
    public CommunityPostResponse createPost(CommunityPostCreateRequest request) {
        CommunityPost post = CommunityPost.builder()
                .userId(request.getUserId())
                .mountainId(request.getMountainId())
                .title(request.getTitle())
                .content(request.getContent())
                .hasImage(request.getImageUrls() != null && !request.getImageUrls().isEmpty())
                .build();

        postRepository.save(post);

        List<CommunityPostImage> images = request.getImageUrls().stream()
                .map(url -> CommunityPostImage.builder()
                        .post(post)
                        .imageUrl(url)
                        .build())
                .collect(Collectors.toList());

        imageRepository.saveAll(images);

        return CommunityPostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .mountainId(post.getMountainId())
                .title(post.getTitle())
                .content(post.getContent())
                .hasImage(post.isHasImage())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                .updatedAt(post.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                .imageUrls(images.stream().map(CommunityPostImage::getImageUrl).collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityPostResponse getPostById(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        String nickname = userClient.getNickname(post.getUserId());

        List<String> imageUrls = imageRepository.findAllByPostId(post.getId()).stream()
                .map(CommunityPostImage::getImageUrl)
                .collect(Collectors.toList());

        return CommunityPostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .nickname(nickname)
                .mountainId(post.getMountainId())
                .title(post.getTitle())
                .content(post.getContent())
                .hasImage(post.isHasImage())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                .updatedAt(post.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                .imageUrls(imageUrls)
                .build();
    }

    public List<CommunityPostResponse> getAllPosts() {
        List<CommunityPost> posts = communityPostRepository.findAll();

        return posts.stream()
                .map(post -> {
                    String nickname = userClient.getNickname(post.getUserId());

                    List<String> imageUrls = imageRepository.findAllByPostId(post.getId())
                            .stream()
                            .map(CommunityPostImage::getImageUrl)
                            .collect(Collectors.toList());

                    return CommunityPostResponse.builder()
                            .id(post.getId())
                            .userId(post.getUserId())
                            .nickname(nickname)
                            .mountainId(post.getMountainId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .hasImage(post.isHasImage())
                            .likeCount(post.getLikeCount())
                            .commentCount(post.getCommentCount())
                            .createdAt(post.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .updatedAt(post.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .imageUrls(imageUrls)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityPostResponse> getRecentPosts(int limit) {
        List<CommunityPost> posts = communityPostRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
        return posts.stream()
                .map(post -> {
                    String nickname = userClient.getNickname(post.getUserId());
                    List<String> imageUrls = imageRepository.findAllByPostId(post.getId())
                            .stream()
                            .map(CommunityPostImage::getImageUrl)
                            .toList();

                    return CommunityPostResponse.builder()
                            .id(post.getId())
                            .userId(post.getUserId())
                            .nickname(nickname)
                            .mountainId(post.getMountainId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .hasImage(post.isHasImage())
                            .likeCount(post.getLikeCount())
                            .commentCount(post.getCommentCount())
                            .createdAt(post.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .updatedAt(post.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .imageUrls(imageUrls)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public CommunityPostResponse updatePost(Long postId, CommunityPostUpdateRequest request) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setMountainId(request.getMountainId());
        post.setHasImage(request.getImageUrls() != null && !request.getImageUrls().isEmpty());

        // 기존 이미지 삭제
        List<CommunityPostImage> oldImages = imageRepository.findAllByPostId(postId);
        imageRepository.deleteAll(oldImages);

        // 새 이미지 저장
        List<CommunityPostImage> newImages = request.getImageUrls() != null
                ? request.getImageUrls().stream()
                .map(url -> CommunityPostImage.builder()
                        .post(post)
                        .imageUrl(url)
                        .build())
                .toList()
                : List.of();
        imageRepository.saveAll(newImages);

        communityPostRepository.save(post);

        return getPostById(postId);
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        List<CommunityPostImage> images = imageRepository.findAllByPostId(postId);
        for (CommunityPostImage image : images) {
            String filename = new File(image.getImageUrl()).getName();
            String filePath = uploadDir + "/" + filename;

            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    System.err.println("이미지 파일 삭제 실패: " + filePath);
                }
            }
        }
        imageRepository.deleteAll(images);
        postRepository.deleteById(postId);
    }

    @Override
    @Transactional
    public void likePost(Long postId, Long userId) {
        if (communityPostLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new IllegalStateException("이미 좋아요를 누른 게시글입니다.");
        }

        // 1. 좋아요 저장
        CommunityPostLike like = CommunityPostLike.builder()
                .postId(postId)
                .userId(userId)
                .build();
        communityPostLikeRepository.save(like);

        // 2. 좋아요 수 증가
        communityPostRepository.incrementLikeCount(postId);

        // 3. ❤️ 좋아요 알림 전송 (자기 좋아요도 알림 보내도록 수정)
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // ✅ 조건 제거 - 자기 좋아요도 알림 전송
        sendLikeNotification(post, userId);
    }

    @Override
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        if (!communityPostLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new IllegalStateException("좋아요를 누르지 않은 게시글입니다.");
        }

        communityPostLikeRepository.deleteByPostIdAndUserId(postId, userId);

        // ✅ 좋아요 수 감소
        communityPostRepository.decrementLikeCount(postId);
    }

    /**
     * ❤️ 좋아요 알림 전송 (자기 좋아요도 알림 보내도록 수정)
     */
    private void sendLikeNotification(CommunityPost post, Long likedUserId) {
        try {
            log.info("❤️ 좋아요 알림 전송 시작: postId={}, postAuthor={}, liker={}",
                    post.getId(), post.getUserId(), likedUserId);

            // 1. 좋아요한 사용자 닉네임 조회
            String likerName = userClient.getNickname(likedUserId);

            // 2. 알림 내용 생성
            String postTitle = post.getTitle();
            if (postTitle.length() > 20) {
                postTitle = postTitle.substring(0, 20) + "...";
            }

            // ✅ 자기 좋아요인 경우와 타인 좋아요인 경우 구분
            String content;
            if (post.getUserId().equals(likedUserId)) {
                // 자기 게시글에 자기가 좋아요
                content = String.format("회원님이 '%s' 게시글에 좋아요를 눌렀습니다.", postTitle);
            } else {
                // 타인이 좋아요
                content = String.format("%s님이 '%s' 게시글에 좋아요를 눌렀습니다.", likerName, postTitle);
            }

            // 3. 알림 요청 생성
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .userId(post.getUserId()) // 게시글 작성자에게 알림 (자기 자신 포함)
                    .type("like")
                    .content(content)
                    .title("❤️ 좋아요 알림")
                    .build();

            // 4. 알림 전송
            notificationServiceClient.sendNotification(notificationRequest);

            log.info("❤️ 좋아요 알림 전송 성공: postId={} → userId={}",
                    post.getId(), post.getUserId());

        } catch (Exception e) {
            log.error("❌ 좋아요 알림 전송 실패: postId={}, error={}",
                    post.getId(), e.getMessage());
            // 알림 실패가 좋아요를 막지 않도록 예외를 잡아서 로그만 남김
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityPostResponse> searchPosts(String sort, String field, String keyword, int page, int size) {
        // 정렬 기준 변환
        Sort sorting;
        if ("popular".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.DESC, "likeCount");
        } else if ("oldest".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.ASC, "createdAt");
        } else {
            sorting = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sorting);

        List<CommunityPost> posts;

        if (keyword == null || keyword.trim().isEmpty()) {
            posts = communityPostRepository.findAll(pageable).getContent();
        } else if ("all".equalsIgnoreCase(field)) {
            // 제목+내용 모두 포함하는 검색
            posts = communityPostRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword, pageable).getContent();
        } else if ("title".equalsIgnoreCase(field)) {
            posts = communityPostRepository.findByTitleContainingIgnoreCase(keyword, pageable).getContent();
        } else if ("content".equalsIgnoreCase(field)) {
            posts = communityPostRepository.findByContentContainingIgnoreCase(keyword, pageable).getContent();
        } else {
            posts = communityPostRepository.findAll(pageable).getContent();
        }

        return posts.stream()
                .map(post -> {
                    String nickname = userClient.getNickname(post.getUserId());
                    List<String> imageUrls = imageRepository.findAllByPostId(post.getId())
                            .stream()
                            .map(CommunityPostImage::getImageUrl)
                            .collect(Collectors.toList());

                    return CommunityPostResponse.builder()
                            .id(post.getId())
                            .userId(post.getUserId())
                            .nickname(nickname)
                            .mountainId(post.getMountainId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .hasImage(post.isHasImage())
                            .likeCount(post.getLikeCount())
                            .commentCount(post.getCommentCount())
                            .createdAt(post.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .updatedAt(post.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .imageUrls(imageUrls)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityPostResponse> getPostsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdAt"));
        List<CommunityPost> posts = communityPostRepository.findByUserId(userId, pageable).getContent();

        return posts.stream()
                .map(post -> {
                    String nickname = userClient.getNickname(post.getUserId());
                    List<String> imageUrls = imageRepository.findAllByPostId(post.getId())
                            .stream()
                            .map(CommunityPostImage::getImageUrl)
                            .collect(Collectors.toList());

                    return CommunityPostResponse.builder()
                            .id(post.getId())
                            .userId(post.getUserId())
                            .nickname(nickname)
                            .mountainId(post.getMountainId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .hasImage(post.isHasImage())
                            .likeCount(post.getLikeCount())
                            .commentCount(post.getCommentCount())
                            .createdAt(post.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .updatedAt(post.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .imageUrls(imageUrls)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPostsByUserWithTotalPages(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CommunityPost> postPage = communityPostRepository.findByUserId(userId, pageable);

        List<CommunityPostResponse> posts = postPage.getContent().stream()
                .map(post -> {
                    String nickname = userClient.getNickname(post.getUserId());
                    List<String> imageUrls = imageRepository.findAllByPostId(post.getId())
                            .stream()
                            .map(CommunityPostImage::getImageUrl)
                            .collect(Collectors.toList());

                    return CommunityPostResponse.builder()
                            .id(post.getId())
                            .userId(post.getUserId())
                            .nickname(nickname)
                            .mountainId(post.getMountainId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .hasImage(post.isHasImage())
                            .likeCount(post.getLikeCount())
                            .commentCount(post.getCommentCount())
                            .createdAt(post.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .updatedAt(post.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .imageUrls(imageUrls)
                            .build();
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", postPage.getTotalPages());
        result.put("posts", posts);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> searchPostsWithTotalPages(String sort, String field, String keyword, int page, int size) {
        Sort sorting;
        if ("popular".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.DESC, "likeCount");
        } else if ("oldest".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.ASC, "createdAt");
        } else {
            sorting = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sorting);

        Page<CommunityPost> postPage;
        if (keyword == null || keyword.trim().isEmpty()) {
            postPage = communityPostRepository.findAll(pageable);
        } else if ("all".equalsIgnoreCase(field)) {
            postPage = communityPostRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword, pageable);
        } else if ("title".equalsIgnoreCase(field)) {
            postPage = communityPostRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else if ("content".equalsIgnoreCase(field)) {
            postPage = communityPostRepository.findByContentContainingIgnoreCase(keyword, pageable);
        } else {
            postPage = communityPostRepository.findAll(pageable);
        }

        List<CommunityPostResponse> posts = postPage.getContent().stream()
                .map(post -> {
                    String nickname = userClient.getNickname(post.getUserId());
                    List<String> imageUrls = imageRepository.findAllByPostId(post.getId())
                            .stream()
                            .map(CommunityPostImage::getImageUrl)
                            .collect(Collectors.toList());

                    return CommunityPostResponse.builder()
                            .id(post.getId())
                            .userId(post.getUserId())
                            .nickname(nickname)
                            .mountainId(post.getMountainId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .hasImage(post.isHasImage())
                            .likeCount(post.getLikeCount())
                            .commentCount(post.getCommentCount())
                            .createdAt(post.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .updatedAt(post.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .imageUrls(imageUrls)
                            .build();
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", postPage.getTotalPages());
        result.put("posts", posts);
        return result;
    }
}