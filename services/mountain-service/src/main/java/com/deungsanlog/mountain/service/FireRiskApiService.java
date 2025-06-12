package com.deungsanlog.mountain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 산림청 산불위험예보 API 호출 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FireRiskApiService {

    private final WebClient webClient;

    @Value("${fire.api.key}")
    private String apiKey;

    @Value("${fire.api.url}")
    private String apiUrl;

    /**
     * 지역별 산불위험예보 조회
     */
    public Map<String, Object> getFireRiskInfo(String location) {
        log.info("산불위험예보 조회 시작: location={}", location);

        try {
            // 산림청 API 호출
            Map<String, Object> apiResponse = callFireRiskApi();

            if (apiResponse == null) {
                return createErrorResponse("API 응답이 null입니다");
            }

            // 응답 파싱
            Map<String, Object> result = parseFireRiskResponse(apiResponse);
            log.info("산불위험예보 조회 성공: {}", result);
            return result;

        } catch (Exception e) {
            log.error("산불위험예보 조회 실패", e);
            return createErrorResponse("산불위험예보 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 산림청 API 호출
     */
    private Map<String, Object> callFireRiskApi() {
        String url = apiUrl +
                "?serviceKey=" + apiKey +
                "&pageNo=1" +
                "&numOfRows=10" +
                "&_type=json";

        log.debug("산림청 API 호출: {}", url);

        try {
            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return response;

        } catch (Exception e) {
            log.error("API 호출 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 산불위험예보 API 응답 파싱 (실제 구조 반영)
     */
    private Map<String, Object> parseFireRiskResponse(Map<String, Object> apiResponse) {
        try {
            // API 응답 구조: response.body.items.item (단일 객체)
            Map<String, Object> response = (Map<String, Object>) apiResponse.get("response");
            if (response == null) {
                return createErrorResponse("response 키가 없습니다");
            }

            Map<String, Object> body = (Map<String, Object>) response.get("body");
            if (body == null) {
                return createErrorResponse("body 키가 없습니다");
            }

            Map<String, Object> items = (Map<String, Object>) body.get("items");
            if (items == null) {
                return createErrorResponse("items 키가 없습니다");
            }

            // item은 단일 객체 (배열이 아님)
            Map<String, Object> item = (Map<String, Object>) items.get("item");
            if (item == null) {
                return createErrorResponse("산불위험예보 데이터가 없습니다");
            }

            log.info("파싱할 item 데이터: {}", item);

            // meanavg 값으로 위험도 계산
            Object meanAvgObj = item.get("meanavg");
            if (meanAvgObj == null) {
                return createErrorResponse("위험도 데이터가 없습니다");
            }

            int meanAvg = Integer.parseInt(meanAvgObj.toString());
            String riskLevel = calculateRiskLevel(meanAvg);
            String riskDescription = getRiskDescription(riskLevel);
            String precautions = getPrecautions(riskLevel);

            Map<String, Object> result = new HashMap<>();
            result.put("riskLevel", riskLevel);
            result.put("riskLevelCode", getRiskLevelCode(riskLevel));
            result.put("meanAvg", meanAvg);
            result.put("description", riskDescription);
            result.put("precautions", precautions);
            //result.put("doname", item.get("doname"));
            result.put("analdate", item.get("analdate"));
            result.put("date", LocalDate.now().toString());
            result.put("success", true);

            log.info("산불위험예보 파싱 완료: meanAvg={}, riskLevel={}", meanAvg, riskLevel);
            return result;

        } catch (Exception e) {
            log.error("산불위험예보 응답 파싱 실패", e);
            return createErrorResponse("산불위험예보 데이터 파싱 실패: " + e.getMessage());
        }
    }

    /**
     * meanavg 값으로 산불 위험도 계산
     */
    private String calculateRiskLevel(int meanAvg) {
        if (meanAvg < 30) {
            return "안전";
        } else if (meanAvg <= 50) {
            return "주의";
        } else {
            return "경보";
        }
    }

    /**
     * 위험도별 설명 메시지
     */
    private String getRiskDescription(String riskLevel) {
        switch (riskLevel) {
            case "안전":
                return "산불 발생 위험이 낮습니다. 안전한 등산이 가능합니다.";
            case "주의":
                return "산불 발생 위험이 보통입니다. 화기 사용에 주의하세요.";
            case "경보":
                return "산불 발생 위험이 높습니다. 등산을 자제하고 화기 사용을 금지하세요.";
            default:
                return "산불 위험도 정보를 확인할 수 없습니다.";
        }
    }

    /**
     * 위험도별 주의사항
     */
    private String getPrecautions(String riskLevel) {
        switch (riskLevel) {
            case "안전":
                return "평상시와 같이 등산하되 기본적인 화기 안전수칙을 준수하세요.";
            case "주의":
                return "담배, 라이터 등 화기물 사용을 자제하고 쓰레기를 버리지 마세요.";
            case "경보":
                return "등산 자제를 권장하며, 화기 사용을 절대 금지합니다. 비상시 119에 즉시 신고하세요.";
            default:
                return "산불 예방을 위해 화기 사용을 주의하세요.";
        }
    }

    /**
     * 위험도별 코드 반환
     */
    private String getRiskLevelCode(String riskLevel) {
        switch (riskLevel) {
            case "안전":
                return "1";
            case "주의":
                return "2";
            case "경보":
                return "3";
            default:
                return "0";
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