package com.deungsanlog.mountain.service;


import com.deungsanlog.mountain.service.CoordinateConversionService.GridCoordinate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${weather.api.key:YOUR_WEATHER_API_KEY}")
    private String apiKey;

    @Value("${weather.api.url:http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst}")
    private String apiUrl;

    /**
     * 산의 위도/경도로 1주일치 날씨 정보 조회
     * @param longitude 경도
     * @param latitude 위도
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
}