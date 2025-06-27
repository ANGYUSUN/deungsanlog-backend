package com.deungsanlog.community.repository;

import com.deungsanlog.community.domain.CommunityPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {


    @Modifying
    @Query("UPDATE CommunityPost p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE CommunityPost p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId AND p.likeCount > 0")
    void decrementLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE CommunityPost p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    void incrementCommentCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE CommunityPost p SET p.commentCount = p.commentCount - 1 WHERE p.id = :postId AND p.commentCount > 0")
    void decrementCommentCount(@Param("postId") Long postId);

    List<CommunityPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<CommunityPost> findAll(Pageable pageable);

    Page<CommunityPost> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    Page<CommunityPost> findByContentContainingIgnoreCase(String keyword, Pageable pageable);

    Page<CommunityPost> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String titleKeyword, String contentKeyword, Pageable pageable);

    Page<CommunityPost> findByUserId(Long userId, Pageable pageable);
}