package com.deungsanlog.mountain.repository;

import com.deungsanlog.mountain.entity.MountainDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MountainDescriptionRepository extends JpaRepository<MountainDescription, Long> {
    // 기존 메서드 (PK로 조회)
    // findById는 이미 JpaRepository에서 제공되므로 별도 선언 불필요

    // 새로 추가 - mountainId (FK)로 조회
    Optional<MountainDescription> findByMountainId(Long mountainId);
}