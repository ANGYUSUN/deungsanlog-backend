package com.deungsanlog.community.service;

import com.deungsanlog.community.client.UserClient;
import com.deungsanlog.community.domain.CommunityPost;
import com.deungsanlog.community.domain.CommunityPostImage;
import com.deungsanlog.community.dto.CommunityPostCreateRequest;
import com.deungsanlog.community.dto.CommunityPostResponse;
import com.deungsanlog.community.repository.CommunityPostImageRepository;
import com.deungsanlog.community.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityPostServiceImpl implements CommunityPostService {

    private final CommunityPostRepository postRepository;
    private final CommunityPostImageRepository imageRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserClient userClient; // 유저 서비스 호출을 위한 클라이언트

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

    public List<CommunityPostResponse> getAllPosts() {
        List<CommunityPost> posts = communityPostRepository.findAll();

        return posts.stream()
                .map(post -> {
                    String nickname = userClient.getNickname(post.getUserId());

                    // 이미지 목록 불러오기
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


}
