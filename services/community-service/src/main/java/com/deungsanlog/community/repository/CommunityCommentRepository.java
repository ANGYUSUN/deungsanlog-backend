package com.deungsanlog.community.repository;

import com.deungsanlog.community.domain.CommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    List<CommunityComment> findByPostId(Long postId);
}