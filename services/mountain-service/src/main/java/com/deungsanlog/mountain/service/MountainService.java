package com.deungsanlog.mountain.service;

import com.deungsanlog.mountain.dto.MountainDetailDto;
import com.deungsanlog.mountain.dto.MountainRecordSearchResponse;
import com.deungsanlog.mountain.entity.Mountain;
import com.deungsanlog.mountain.entity.MountainDescription;
import com.deungsanlog.mountain.entity.MountainSunInfo;
import com.deungsanlog.mountain.repository.MountainDescriptionRepository;
import com.deungsanlog.mountain.repository.MountainRepository;
import com.deungsanlog.mountain.repository.MountainSunInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class MountainService {

    @Autowired
    private MountainRepository mountainRepository;

    @Autowired
    private MountainDescriptionRepository mountainDescriptionRepository;

    @Autowired
    private MountainSunInfoRepository mountainSunInfoRepository;

    // ===== 실시간 API 서비스 의존성 추가 =====

    @Autowired
    private WeatherApiService weatherApiService;

    @Autowired
    private FireRiskApiService fireRiskApiService;

    /**
     * 산 이름으로 상세 정보 조회 (기존 메서드 + 실시간 데이터 추가)
     *
     * @param name 산 이름
     * @return 산 상세 정보 (기본정보 + 설명 + 일출일몰 + 실시간 날씨 + 산불위험예보)
     */
    public MountainDetailDto searchMountain(String name) {
        // 1. 산 기본 정보 조회
        List<Mountain> mountains = mountainRepository.findByName(name);
        if (mountains.isEmpty()) {
            throw new RuntimeException("산을 찾을 수 없습니다: " + name);
        }
        Mountain mountain = mountains.get(0);

        // 2. 산 설명 조회 (mountainId로 조회) + HTML 디코딩
        MountainDescription description = mountainDescriptionRepository
                .findByMountainId(mountain.getId()).orElse(null);

        // HTML 엔티티 디코딩
        if (description != null) {
            description = decodeHtmlEntities(description);
        }

        // 3. 오늘 일출/일몰 정보 조회 (mountainId와 date로 조회)
        MountainSunInfo sunInfo = mountainSunInfoRepository
                .findByMountainIdAndDate(mountain.getId(), LocalDate.now()).orElse(null);

        // 4. 실시간 날씨 정보 조회
        Map<String, Object> weatherInfo = getWeatherInfo(mountain);

        // 5. 산불위험예보 정보 조회
        Map<String, Object> fireRiskInfo = getFireRiskInfo(mountain);

        // 6. 모든 데이터를 하나로 합쳐서 반환
        return new MountainDetailDto(mountain, description, sunInfo, weatherInfo, fireRiskInfo);
    }

    /**
     * 실시간 날씨 정보 조회
     */
    private Map<String, Object> getWeatherInfo(Mountain mountain) {
        try {
            if (mountain.getLatitude() != null && mountain.getLongitude() != null) {
                return weatherApiService.getCurrentWeather(mountain.getLongitude(), mountain.getLatitude());
            } else {
                return Map.of(
                        "error", true,
                        "message", "산의 좌표 정보가 없습니다"
                );
            }
        } catch (Exception e) {
            return Map.of(
                    "error", true,
                    "message", "날씨 정보 조회 실패: " + e.getMessage()
            );
        }
    }

    /**
     * 산불위험예보 정보 조회
     */
    private Map<String, Object> getFireRiskInfo(Mountain mountain) {
        try {
            if (mountain.getLocation() != null) {
                return fireRiskApiService.getFireRiskInfo(mountain.getLocation());
            } else {
                return Map.of(
                        "error", true,
                        "message", "산의 지역 정보가 없습니다"
                );
            }
        } catch (Exception e) {
            return Map.of(
                    "error", true,
                    "message", "산불위험예보 조회 실패: " + e.getMessage()
            );
        }
    }

    /**
     * MountainDescription의 HTML 엔티티 디코딩 + 완전한 텍스트 정리
     */
    private MountainDescription decodeHtmlEntities(MountainDescription description) {
        if (description.getHikingCourseInfo() != null) {
            description.setHikingCourseInfo(cleanHtmlText(description.getHikingCourseInfo()));
        }
        if (description.getFullDescription() != null) {
            description.setFullDescription(cleanHtmlText(description.getFullDescription()));
        }
        if (description.getHikingPointInfo() != null) {
            description.setHikingPointInfo(cleanHtmlText(description.getHikingPointInfo()));
        }
        if (description.getTransportInfo() != null) {
            description.setTransportInfo(cleanHtmlText(description.getTransportInfo()));
        }
        if (description.getNearbyTourInfo() != null) {
            description.setNearbyTourInfo(cleanHtmlText(description.getNearbyTourInfo()));
        }
        if (description.getSummary() != null) {
            description.setSummary(cleanHtmlText(description.getSummary()));
        }
        if (description.getDifficulty() != null) {
            description.setDifficulty(cleanHtmlText(description.getDifficulty()));
        }
        return description;
    }

    /**
     * HTML 텍스트 완전 정리 메서드
     */
    private String cleanHtmlText(String htmlText) {
        if (htmlText == null || htmlText.trim().isEmpty()) {
            return htmlText;
        }

        String cleaned = htmlText;

        // 1. HTML 엔티티 디코딩
        cleaned = HtmlUtils.htmlUnescape(cleaned);

        // 2. 탭 문자 제거
        cleaned = cleaned.replaceAll("\\t", "");

        // 3. 캐리지 리턴 제거
        cleaned = cleaned.replaceAll("\\r", "");

        // 4. 연속된 줄바꿈을 하나로
        cleaned = cleaned.replaceAll("\\n+", "\\n");

        // 5. HTML 태그 사이의 불필요한 공백 제거
        cleaned = cleaned.replaceAll(">\\s+<", "><");

        // 6. HTML 태그 앞뒤 공백 정리
        cleaned = cleaned.replaceAll("\\s+>", ">");
        cleaned = cleaned.replaceAll("<\\s+", "<");

        // 7. 연속된 공백을 하나로 (단, 줄바꿈은 보존)
        cleaned = cleaned.replaceAll("[ \\u00A0]+", " "); // 일반 공백과 non-breaking space 처리

        // 8. 줄바꿈 앞뒤 공백 제거
        cleaned = cleaned.replaceAll(" *\\n *", "\\n");

        // 9. <br> 태그 주변 정리
        cleaned = cleaned.replaceAll("\\s*<br>\\s*", "<br>");
        cleaned = cleaned.replaceAll("\\s*<br/>\\s*", "<br>");
        cleaned = cleaned.replaceAll("\\s*<br />\\s*", "<br>");

        // 10. 특정 HTML 태그 뒤의 공백 정리
        cleaned = cleaned.replaceAll("</td>\\s+", "</td>");
        cleaned = cleaned.replaceAll("</th>\\s+", "</th>");
        cleaned = cleaned.replaceAll("</tr>\\s+", "</tr>");

        // 11. 문장 끝의 불필요한 공백 제거
        cleaned = cleaned.replaceAll("\\s+\\.", ".");
        cleaned = cleaned.replaceAll("\\s+,", ",");
        cleaned = cleaned.replaceAll("\\s+:", ":");
        cleaned = cleaned.replaceAll("\\s+;", ";");

        // 12. 대시(-) 주변 공백 정리
        cleaned = cleaned.replaceAll("\\s+-\\s+", " - ");

        // 13. 괄호 앞뒤 공백 정리
        cleaned = cleaned.replaceAll("\\s+\\(", " (");
        cleaned = cleaned.replaceAll("\\)\\s+", ") ");

        // 14. 전체 앞뒤 공백 제거
        cleaned = cleaned.trim();

        return cleaned;
    }

    // ===== 기존 메서드들 (다른 용도로 필요할 수 있음) =====

    /**
     * 1. 산 이름으로 검색 (기본 정보만, 여러 결과 가능)
     */
    public List<Mountain> searchMountainsByName(String name) {
        List<Mountain> mountains = mountainRepository.findByName(name);
        if (mountains.isEmpty()) {
            throw new RuntimeException("산을 찾을 수 없습니다: " + name);
        }
        return mountains;
    }

    /**
     * 2. 산 ID로 전체 상세 정보 조회
     */
    public MountainDetailDto getMountainDetail(Long mountainId) {
        // 산 기본 정보
        Mountain mountain = mountainRepository.findById(mountainId)
                .orElseThrow(() -> new RuntimeException("산을 찾을 수 없습니다. ID: " + mountainId));

        // 산 설명 정보 (mountainId로 조회) + HTML 디코딩
        MountainDescription description = mountainDescriptionRepository
                .findByMountainId(mountainId).orElse(null);

        // HTML 엔티티 디코딩
        if (description != null) {
            description = decodeHtmlEntities(description);
        }

        // 오늘 일출/일몰 정보 (mountainId와 date로 조회)
        MountainSunInfo sunInfo = mountainSunInfoRepository
                .findByMountainIdAndDate(mountainId, LocalDate.now()).orElse(null);

        // 실시간 정보 추가
        Map<String, Object> weatherInfo = getWeatherInfo(mountain);
        Map<String, Object> fireRiskInfo = getFireRiskInfo(mountain);

        return new MountainDetailDto(mountain, description, sunInfo, weatherInfo, fireRiskInfo);
    }

    /**
     * 3. 산 기본 정보만 조회
     */
    public Mountain getMountainBasic(Long mountainId) {
        return mountainRepository.findById(mountainId)
                .orElseThrow(() -> new RuntimeException("산을 찾을 수 없습니다. ID: " + mountainId));
    }

    // 레코드 서비스에 제공하는 산 검색 기능
    public List<MountainRecordSearchResponse> searchByKeyword(String keyword) {
        return mountainRepository.findByNameOrLocationContaining(keyword).stream()
                .map(m -> new MountainRecordSearchResponse(m.getId(), m.getName(), m.getLocation()))
                .toList();
    }
}