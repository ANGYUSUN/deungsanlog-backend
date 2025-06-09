package com.deungsanlog.record.repository;

import com.deungsanlog.record.domain.RecordHiking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordHikingRepository extends JpaRepository<RecordHiking, Long> {
    int countByUserId(Long userId);
    List<RecordHiking> findByUserId(Long userId);
}
