package com.deungsanlog.mountain.repository;

import com.deungsanlog.mountain.entity.MountainSunInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MountainSunInfoRepository extends JpaRepository<MountainSunInfo, Long> {
    Optional<MountainSunInfo> findByDate(LocalDate date); // 기존 메서드 유지

    Optional<MountainSunInfo> findByMountainIdAndDate(Long mountainId, LocalDate date); // 새로 추가!
    //특정 날짜 범위의 일출몰 모두 조회.
    //List<MountainSunInfo> findByDateBetween(LocalDate startDate, LocalDate endDate);
}