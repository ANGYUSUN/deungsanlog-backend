//3개 엔티티를 하나로 묶어주는 역할
//Service에서 3개 Repository 조회 결과를 담아서 Controller로 전달
//Lombok으로 getter/setter 자동 생성package com.deungsanlog.mountain.dto;


package com.deungsanlog.mountain.dto;

import com.deungsanlog.mountain.entity.Mountain;
import com.deungsanlog.mountain.entity.MountainDescription;
import com.deungsanlog.mountain.entity.MountainSunInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 산 상세 정보 통합 DTO
 * 기존 DB 데이터 + 실시간 API 데이터 포함
 */
@Getter
@Setter
@NoArgsConstructor
public class MountainDetailDto {

    // ===== 기존 DB 데이터 =====

    // 산 기본 정보
    private Mountain mountain;

    // 산 상세 설명
    private MountainDescription description;

    // 일출/일몰 정보
    private MountainSunInfo sunInfo;

    // ===== 실시간 API 데이터 =====

    // 실시간 날씨 정보
    private Map<String, Object> weatherInfo;

    // 산불위험예보 정보
    private Map<String, Object> fireRiskInfo;

    // ===== 생성자 =====

    /**
     * 기존 DB 데이터만 포함하는 생성자 (기존 코드 호환성)
     */
    public MountainDetailDto(Mountain mountain, MountainDescription description, MountainSunInfo sunInfo) {
        this.mountain = mountain;
        this.description = description;
        this.sunInfo = sunInfo;
        this.weatherInfo = null;
        this.fireRiskInfo = null;
    }

    /**
     * 모든 데이터를 포함하는 생성자 (새로운 기능)
     */
    public MountainDetailDto(Mountain mountain, MountainDescription description, MountainSunInfo sunInfo,
                             Map<String, Object> weatherInfo, Map<String, Object> fireRiskInfo) {
        this.mountain = mountain;
        this.description = description;
        this.sunInfo = sunInfo;
        this.weatherInfo = weatherInfo;
        this.fireRiskInfo = fireRiskInfo;
    }
}