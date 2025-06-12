package com.deungsanlog.mountain.service;


import com.deungsanlog.mountain.service.CoordinateConversionService.GridCoordinate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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

    @Value("${weather.api.url:http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst}")
    private String apiUrl;

    /**
     * 산의 위도/경도로 실시간 날씨 정보 조회
     * @param longitude 경도
     * @param latitude 위도
     * @return 날씨 정보
     */
    public Map<String, Object> getCurrentWeather(double longitude, double latitude) {
        log.info("날씨 정보 조회 시작: lon={}, lat={}", longitude, latitude);

        try {
            // 1. 위도/경도를 기상청 격자좌표로 변환
            GridCoordinate grid = coordinateService.convertToGrid(longitude, latitude);
            log.info("격자 좌표 변환 완료: {}", grid);

            // 2. 현재 시간 기준으로 API 파라미터 생성
            LocalDateTime now = LocalDateTime.now();
            String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String baseTime = getBaseTime(now);

            // 3. 기상청 API 호출
            Map<String, Object> apiResponse = callWeatherApi(grid.x, grid.y, baseDate, baseTime);

            // 4. 응답 파싱
            Map<String, Object> result = parseWeatherResponse(apiResponse);
            log.info("날씨 조회 성공: {}", result);
            return result;

        } catch (Exception e) {
            log.error("날씨 조회 중 오류 발생", e);
            return createErrorResponse("날씨 정보 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 기상청 API 호출
     */
    private Map<String, Object> callWeatherApi(int nx, int ny, String baseDate, String baseTime) {
        String url = apiUrl +
                "?serviceKey=" + apiKey +
                "&numOfRows=10" +
                "&pageNo=1" +
                "&dataType=JSON" +
                "&base_date=" + baseDate +
                "&base_time=" + baseTime +
                "&nx=" + nx +
                "&ny=" + ny;

        log.debug("기상청 API 호출: {}", url);

        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(); // 동기 방식으로 변경
        } catch (Exception e) {
            log.error("기상청 API 호출 실패", e);
            return createErrorResponse("API 호출 실패");
        }
    }

    /**
     * 기상청 API 응답 파싱
     */
    private Map<String, Object> parseWeatherResponse(Map<String, Object> apiResponse) {
        try {
            // TODO: 실제 API 응답 구조에 맞게 파싱 로직 구현
            // 현재는 임시 데이터 반환
            Map<String, Object> result = new HashMap<>();
            result.put("temperature", "15°C");
            result.put("humidity", "60%");
            result.put("windSpeed", "2.5m/s");
            result.put("precipitation", "0mm");
            result.put("weather", "맑음");
            result.put("updateTime", LocalDateTime.now().toString());
            result.put("success", true);

            return result;

        } catch (Exception e) {
            log.error("날씨 응답 파싱 실패", e);
            return createErrorResponse("날씨 데이터 파싱 실패");
        }
    }

    /**
     * 기상청 API용 baseTime 계산
     * 기상청은 특정 시간대에만 데이터를 제공 (매시 30분 이후)
     */
    private String getBaseTime(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();

        // 30분 이전이면 이전 시간대 데이터 조회
        if (minute < 30) {
            hour = hour - 1;
            if (hour < 0) hour = 23;
        }

        return String.format("%02d30", hour);
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