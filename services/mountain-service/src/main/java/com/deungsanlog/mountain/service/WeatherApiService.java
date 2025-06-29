package com.deungsanlog.mountain.service;


import com.deungsanlog.mountain.client.FavoriteServiceClient;
import com.deungsanlog.mountain.client.NotificationServiceClient;
import com.deungsanlog.mountain.dto.BulkNotificationRequest;
import com.deungsanlog.mountain.entity.Mountain;
import com.deungsanlog.mountain.repository.MountainRepository;
import com.deungsanlog.mountain.service.CoordinateConversionService.GridCoordinate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 기상청 단기예보 API 호출 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherApiService {

    private final WebClient webClient;
    private final CoordinateConversionService coordinateService;

    // 🌧️ 알림 서비스 클라이언트 추가 (기존 의존성 그대로 사용)
    private final FavoriteServiceClient favoriteServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final MountainRepository mountainRepository;

    @Value("${weather.api.key:YOUR_WEATHER_API_KEY}")
    private String apiKey;

    @Value("${weather.api.url:http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst}")
    private String apiUrl;

    /**
     * 산의 위도/경도로 1주일치 날씨 정보 조회
     *
     * @param longitude 경도
     * @param latitude  위도
     * @return 1주일치 날씨 정보
     */
    public Map<String, Object> getCurrentWeather(double longitude, double latitude) {
        log.info("1주일치 날씨 정보 조회 시작: lon={}, lat={}", longitude, latitude);

        try {
            // 1. 위도/경도를 기상청 격자좌표로 변환
            GridCoordinate grid = coordinateService.convertToGrid(longitude, latitude);
            log.info("격자 좌표 변환 완료: {}", grid);

            // 2. 1주일치 날씨 데이터 수집
            List<Map<String, Object>> weeklyWeather = getWeeklyWeather(grid.x, grid.y);

            // 3. 결과 구성
            Map<String, Object> result = new HashMap<>();
            result.put("weeklyWeather", weeklyWeather);
            result.put("currentWeather", weeklyWeather.get(0)); // 오늘 날씨를 현재 날씨로
            result.put("success", true);
            result.put("updateTime", LocalDateTime.now().toString());

            log.info("1주일치 날씨 조회 성공: {}일 데이터", weeklyWeather.size());
            return result;

        } catch (Exception e) {
            log.error("날씨 조회 중 오류 발생", e);
            return createErrorResponse("날씨 정보 조회 실패: " + e.getMessage());
        }
    }

    // ========== 🆕 NEW: 날씨 알림 기능 추가 ==========

    /**
     * 🌧️ 악천후 알림 체크 및 전송
     */
    private void checkAndSendWeatherAlert(double longitude, double latitude, Map<String, Object> weatherData) {
        try {
            log.debug("🌧️ 악천후 알림 체크 시작: lon={}, lat={}", longitude, latitude);

            // 1. 해당 좌표 근처의 산들 찾기
            List<Mountain> nearbyMountains = findNearbyMountains(longitude, latitude);

            if (nearbyMountains.isEmpty()) {
                log.debug("📍 해당 좌표 근처에 등록된 산이 없습니다");
                return;
            }

            // 2. 악천후 조건 체크
            WeatherAlert alert = checkWeatherConditions(weatherData);

            if (alert.isAlert()) {
                // 3. 각 산별로 즐겨찾기 사용자들에게 알림 전송
                for (Mountain mountain : nearbyMountains) {
                    sendWeatherAlertForMountain(mountain, alert);
                }
            }

        } catch (Exception e) {
            log.error("❌ 악천후 알림 체크 실패: lon={}, lat={}", longitude, latitude, e);
            // 알림 실패가 날씨 조회를 막지 않도록 예외를 잡아서 로그만 남김
        }
    }

    /**
     * 좌표 근처의 산들 찾기 (간단한 구현)
     */
    private List<Mountain> findNearbyMountains(double longitude, double latitude) {
        try {
            // 🔍 좌표 기반으로 근처 산 검색 (간단한 범위 검색)
            return mountainRepository.findAll().stream()
                    .filter(mountain -> mountain.getLatitude() != null && mountain.getLongitude() != null)
                    .filter(mountain -> {
                        double distance = calculateDistance(longitude, latitude,
                                mountain.getLongitude(), mountain.getLatitude());
                        return distance <= 10.0; // 10km 반경 내
                    })
                    .toList();
        } catch (Exception e) {
            log.error("❌ 근처 산 검색 실패", e);
            return List.of();
        }
    }

    /**
     * 간단한 거리 계산 (km)
     */
    private double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        double R = 6371; // 지구 반지름 (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * 1주일치 날씨 데이터 수집
     */
    private List<Map<String, Object>> getWeeklyWeather(int nx, int ny) {
        List<Map<String, Object>> weeklyData = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 7일치 데이터 생성 (실제로는 기상청 API에서 3일치만 제공하므로 임시 데이터로 구성)
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            Map<String, Object> dayWeather = createDayWeather(date, i);
            weeklyData.add(dayWeather);
        }

        return weeklyData;
    }

    /**
     * 하루 날씨 데이터 생성
     */
    private Map<String, Object> createDayWeather(LocalDate date, int dayOffset) {
        Map<String, Object> dayWeather = new HashMap<>();

        // 날짜 정보
        dayWeather.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dayWeather.put("dayOfWeek", getDayOfWeek(date));

        // 날씨 정보 (임시 데이터 - 실제로는 API에서 가져와야 함)
        if (dayOffset == 0) {
            // 오늘
            dayWeather.put("temperature", "15°C");
            dayWeather.put("humidity", "60%");
            dayWeather.put("windSpeed", "2.5m/s");
            dayWeather.put("precipitation", "0mm");
            dayWeather.put("weather", "맑음");
        } else if (dayOffset == 1) {
            // 내일
            dayWeather.put("temperature", "18°C");
            dayWeather.put("humidity", "55%");
            dayWeather.put("windSpeed", "3.0m/s");
            dayWeather.put("precipitation", "5mm");
            dayWeather.put("weather", "구름많음");
        } else if (dayOffset == 2) {
            // 모레
            dayWeather.put("temperature", "20°C");
            dayWeather.put("humidity", "70%");
            dayWeather.put("windSpeed", "4.0m/s");
            dayWeather.put("precipitation", "15mm");
            dayWeather.put("weather", "비");
        } else {
            // 나머지 날들
            dayWeather.put("temperature", (15 + dayOffset) + "°C");
            dayWeather.put("humidity", (60 + dayOffset * 2) + "%");
            dayWeather.put("windSpeed", (2.5 + dayOffset * 0.5) + "m/s");
            dayWeather.put("precipitation", (dayOffset * 3) + "mm");
            dayWeather.put("weather", dayOffset % 2 == 0 ? "맑음" : "구름많음");
        }

        return dayWeather;
    }

    /**
     * 요일 반환
     */
    private String getDayOfWeek(LocalDate date) {
        String[] days = {"일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"};
        return days[date.getDayOfWeek().getValue() % 7];
    }

    /**
     * 기상청 API 호출 (단기예보)
     * 악천후 조건 체크
     */
    private WeatherAlert checkWeatherConditions(Map<String, Object> weatherData) {
        try {
            // 임시 데이터에서 값 추출 (실제 API 파싱 후 수정 필요)
            String weather = (String) weatherData.getOrDefault("weather", "맑음");
            String precipitation = (String) weatherData.getOrDefault("precipitation", "0mm");
            String windSpeed = (String) weatherData.getOrDefault("windSpeed", "0m/s");

            // 강수량 추출 (예: "5mm" → 5)
            double precipitationValue = extractNumericValue(precipitation);
            // 풍속 추출 (예: "15m/s" → 15)
            double windSpeedValue = extractNumericValue(windSpeed);

            // 악천후 조건 체크
            boolean isHeavyRain = precipitationValue >= 10.0; // 10mm 이상
            boolean isStrongWind = windSpeedValue >= 10.0;    // 10m/s 이상
            boolean isBadWeather = weather.contains("폭우") || weather.contains("태풍") ||
                    weather.contains("눈보라") || weather.contains("우박");

            if (isHeavyRain || isStrongWind || isBadWeather) {
                String alertMessage = createWeatherAlertMessage(isHeavyRain, isStrongWind, isBadWeather, weather);
                return new WeatherAlert(true, alertMessage, "weather_alert");
            }

            return new WeatherAlert(false, "", "");

        } catch (Exception e) {
            log.error("❌ 악천후 조건 체크 실패", e);
            return new WeatherAlert(false, "", "");
        }
    }

    /**
     * 숫자 값 추출 ("10mm" → 10.0)
     */
    private double extractNumericValue(String value) {
        try {
            if (value == null || value.isEmpty()) return 0.0;
            return Double.parseDouble(value.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 악천후 알림 메시지 생성
     */
    private String createWeatherAlertMessage(boolean isHeavyRain, boolean isStrongWind,
                                             boolean isBadWeather, String weather) {
        if (isBadWeather) {
            return String.format("⚠️ %s 예보입니다. 등산을 중단하고 안전한 곳으로 대피하세요!", weather);
        } else if (isHeavyRain && isStrongWind) {
            return "⚠️ 폭우와 강풍이 예상됩니다. 등산을 취소하고 실내에 머물러 주세요!";
        } else if (isHeavyRain) {
            return "🌧️ 폭우 경보입니다. 등산로가 위험할 수 있으니 등산을 연기해 주세요!";
        } else if (isStrongWind) {
            return "💨 강풍 경보입니다. 능선 등반은 매우 위험하니 등산을 피해주세요!";
        }
        return "⚠️ 악천후가 예상됩니다. 등산 계획을 재검토해 주세요!";
    }

    /**
     * 특정 산의 즐겨찾기 사용자들에게 날씨 알림 전송
     */
    private void sendWeatherAlertForMountain(Mountain mountain, WeatherAlert alert) {
        try {
            // 1. ✅ 기존 API 경로 사용: getFavoriteUserIds()
            List<Long> favoriteUserIds = favoriteServiceClient.getFavoriteUserIds(mountain.getId());

            if (favoriteUserIds.isEmpty()) {
                log.debug("🔍 {}을(를) 즐겨찾기한 사용자가 없습니다", mountain.getName());
                return;
            }

            // 2. 알림 내용 생성
            String alertContent = String.format("%s 지역에 %s", mountain.getName(), alert.getMessage());

            // 3. ✅ 기존 DTO 사용: BulkNotificationRequest
            BulkNotificationRequest request = BulkNotificationRequest.builder()
                    .userIds(favoriteUserIds)
                    .type(alert.getType())
                    .mountainId(mountain.getId())
                    .mountainName(mountain.getName())
                    .content(alertContent)
                    .title("🌧️ 날씨 경보")
                    .build();

            // 4. ✅ 기존 클라이언트 사용: NotificationServiceClient
            ResponseEntity<?> response = notificationServiceClient.sendBulkNotification(request);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("🌧️ 날씨 알림 전송 성공: {} → {}명", mountain.getName(), favoriteUserIds.size());
            } else {
                log.error("❌ 날씨 알림 전송 실패: {} → 응답코드 {}",
                        mountain.getName(), response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 산별 날씨 알림 전송 실패: mountainId={}, mountainName={}",
                    mountain.getId(), mountain.getName(), e);
        }
    }

    /**
     * ✅ 기존 메서드 그대로 유지
     * 기상청 API 호출
     */
    private Map<String, Object> callWeatherApi(int nx, int ny, String baseDate, String baseTime) {
        String url = apiUrl +
                "?serviceKey=" + apiKey +
                "&numOfRows=100" + // 더 많은 데이터 요청
                "&pageNo=1" +
                "&dataType=JSON" +
                "&base_date=" + baseDate +
                "&base_time=0500" + // 단기예보는 0500, 1100, 1700, 2300에 발표
                "&nx=" + nx +
                "&ny=" + ny;

        log.debug("기상청 단기예보 API 호출: {}", url);

        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.error("기상청 API 호출 실패", e);
            return createErrorResponse("API 호출 실패");
        }
    }

    /**
     * 에러 응답 생성
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", message);
        return errorResponse;
    }

    /**
     * 날씨 알림 정보 클래스
     */
    private static class WeatherAlert {
        private final boolean isAlert;
        private final String message;
        private final String type;

        public WeatherAlert(boolean isAlert, String message, String type) {
            this.isAlert = isAlert;
            this.message = message;
            this.type = type;
        }

        public boolean isAlert() {
            return isAlert;
        }

        public String getMessage() {
            return message;
        }

        public String getType() {
            return type;
        }
    }
}