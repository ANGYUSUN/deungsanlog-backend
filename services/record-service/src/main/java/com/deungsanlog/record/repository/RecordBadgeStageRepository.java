package com.deungsanlog.record.repository;

import com.deungsanlog.record.domain.RecordBadgeStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecordBadgeStageRepository extends JpaRepository<RecordBadgeStage, Integer> {
    Optional<RecordBadgeStage> findByStage(int stage);
}
