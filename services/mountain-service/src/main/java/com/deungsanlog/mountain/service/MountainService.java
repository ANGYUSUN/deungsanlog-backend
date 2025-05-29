package com.deungsanlog.mountain.service;

import com.deungsanlog.mountain.dto.MountainDetailDto;
import com.deungsanlog.mountain.entity.Mountain;
import com.deungsanlog.mountain.entity.MountainDescription;
import com.deungsanlog.mountain.entity.MountainSunInfo;
import com.deungsanlog.mountain.repository.MountainRepository;
import com.deungsanlog.mountain.repository.MountainDescriptionRepository;
import com.deungsanlog.mountain.repository.MountainSunInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MountainService {

    @Autowired
    private MountainRepository mountainRepository;

    @Autowired
    private MountainDescriptionRepository mountainDescriptionRepository;

    @Autowired
    private MountainSunInfoRepository mountainSunInfoRepository;

    /**
     * 산 이름으로 상세 정보 조회
     * @param name 산 이름
     * @return 산 상세 정보 (기본정보 + 설명 + 일출일몰)
     */
    public MountainDetailDto searchMountain(String name) {
        // 1. 산 기본 정보 조회
        List<Mountain> mountains = mountainRepository.findByName(name);
        if (mountains.isEmpty()) {
            throw new RuntimeException("산을 찾을 수 없습니다: " + name);
        }
        Mountain mountain = mountains.get(0);

        // 2. 산 설명 조회 (같은 id 사용)
        MountainDescription description = mountainDescriptionRepository
                .findById(mountain.getId()).orElse(null);

        // 3. 오늘 일출/일몰 정보 조회
        MountainSunInfo sunInfo = mountainSunInfoRepository
                .findByDate(LocalDate.now()).orElse(null);

        // 4. 3개 데이터를 하나로 합쳐서 반환
        return new MountainDetailDto(mountain, description, sunInfo);
    }
}