package com.deungsanlog.community.repository;

import com.deungsanlog.community.domain.CommunityPostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityPostImageRepository extends JpaRepository<CommunityPostImage, Long> {
    List<CommunityPostImage> findAllByPostId(Long postId);
}
