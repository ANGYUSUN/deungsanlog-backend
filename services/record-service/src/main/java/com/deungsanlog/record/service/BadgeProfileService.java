package com.deungsanlog.record.service;

import com.deungsanlog.record.domain.RecordBadgeStage;
import com.deungsanlog.record.dto.BadgeProfileDto;
import com.deungsanlog.record.repository.RecordBadgeStageRepository;
import com.deungsanlog.record.repository.RecordHikingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BadgeProfileService {

    private final RecordHikingRepository recordHikingRepository;
    private final RecordBadgeStageRepository badgeStageRepository;

    public BadgeProfileDto getBadgeProfile(Long userId) {
        int recordCount = recordHikingRepository.countByUserId(userId);
        int stage = calculateStage(recordCount);

        RecordBadgeStage badgeStage = badgeStageRepository.findByStage(stage)
                .orElseThrow(() -> new IllegalArgumentException("해당 단계의 뱃지를 찾을 수 없습니다."));

        String nickname = "테스트유저"; // ✅ 임시값 또는 추후 외부 연동

        return BadgeProfileDto.builder()
                .stage(badgeStage.getStage())
                .title(badgeStage.getTitle())
                .description(badgeStage.getDescription())
                .nickname(nickname)
                .build();
    }

    private int calculateStage(int count) {
        if (count >= 50) return 9;
        if (count >= 30) return 8;
        if (count >= 20) return 7;
        if (count >= 15) return 6;
        if (count >= 10) return 5;
        if (count >= 5) return 4;
        if (count >= 3) return 3;
        if (count >= 1) return 2;
        return 1;
    }
}
