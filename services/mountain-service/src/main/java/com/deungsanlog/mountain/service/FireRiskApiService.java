package com.deungsanlog.mountain.service;

import com.deungsanlog.mountain.client.FavoriteServiceClient;
import com.deungsanlog.mountain.client.NotificationServiceClient;
import com.deungsanlog.mountain.dto.BulkNotificationRequest;
import com.deungsanlog.mountain.entity.Mountain;
import com.deungsanlog.mountain.repository.MountainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 산림청 산불위험예보 API 호출 서비스 + 알림 발송
 */
@Service
@Slf4j
@RequiredArgsConstructor


public class FireRiskApiService {

    private final WebClient webClient;

    // ✅ 변경된 의존성들
    private final FavoriteServiceClient favoriteServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final MountainRepository mountainRepository;

    @Value("${fire.api.key}")
    private String apiKey;

    @Value("${fire.api.url}")
    private String apiUrl;

    /**
     * 지역별 산불위험예보 조회 + 알림 발송
     */
    public Map<String, Object> getFireRiskInfo(String location) {
        log.info("🔥 산불위험예보 조회 시작: location={}", location);

        try {
            // 산림청 API 호출
            Map<String, Object> apiResponse = callFireRiskApi();

            if (apiResponse == null) {
                log.error("❌ API 응답이 null입니다");
                return createErrorResponse("API 응답이 null입니다");
            }

            // 에러 응답인지 확인
            if (apiResponse.containsKey("error") && (Boolean) apiResponse.get("error")) {
                log.error("❌ API 호출 실패: {}", apiResponse.get("message"));
                return apiResponse;
            }

            // 응답 파싱
            Map<String, Object> result = parseFireRiskResponse(apiResponse);

            // 에러 응답인지 확인
            if (result.containsKey("error") && (Boolean) result.get("error")) {
                log.error("❌ 응답 파싱 실패: {}", result.get("message"));
                return result;
            }

            // 🔥 산불 위험도가 높을 때 알림 전송
            if (result.containsKey("riskLevelCode") && result.containsKey("success")) {
                String riskLevelCode = result.get("riskLevelCode").toString();

                // 경보 단계(코드 3)일 때만 알림 전송
                if ("3".equals(riskLevelCode)) {
                    sendFireRiskAlert(location, result);
                }
            }

            log.info("✅ 산불위험예보 조회 성공: {}", result);
            return result;

        } catch (Exception e) {
            log.error("❌ 산불위험예보 조회 실패: {}", e.getMessage(), e);
            return createErrorResponse("산불위험예보 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 🔥 산불 위험 알림 전송
     */
    private void sendFireRiskAlert(String location, Map<String, Object> riskData) {
        try {
            log.info("🚨 산불 위험 알림 전송 시작: location={}", location);

            // 1. 해당 지역의 산들 조회
            List<Mountain> mountainsInLocation = mountainRepository.findByNameOrLocationContaining(location);

            if (mountainsInLocation.isEmpty()) {
                log.info("📍 해당 지역에 등록된 산이 없습니다: {}", location);
                return;
            }

            String riskLevel = riskData.get("riskLevel").toString();
            String description = riskData.get("description").toString();

            // 2. 각 산별로 즐겨찾기 사용자들에게 알림 전송
            for (Mountain mountain : mountainsInLocation) {
                try {
                    sendAlertForMountain(mountain, riskLevel, description);
                } catch (Exception e) {
                    log.error("❌ 산별 알림 전송 실패: mountainId={}, mountainName={}",
                            mountain.getId(), mountain.getName(), e);
                }
            }

            log.info("✅ 산불 위험 알림 전송 완료: location={}, 대상 산 {}개",
                    location, mountainsInLocation.size());

        } catch (Exception e) {
            log.error("❌ 산불 위험 알림 전송 중 오류: location={}", location, e);
        }
    }

    /**
     * 특정 산의 즐겨찾기 사용자들에게 알림 전송
     */
    private void sendAlertForMountain(Mountain mountain, String riskLevel, String description) {
        try {
            // ✅ favoriteServiceClient 사용
            List<Long> favoriteUserIds = favoriteServiceClient.getFavoriteUserIds(mountain.getId());

            if (favoriteUserIds.isEmpty()) {
                log.debug("🔍 {}을(를) 즐겨찾기한 사용자가 없습니다", mountain.getName());
                return;
            }

            // 2. 알림 내용 생성
            String alertContent = String.format(
                    "%s 지역 산불위험도가 '%s'로 상승했습니다. %s",
                    mountain.getName(),
                    riskLevel,
                    getSimpleRiskMessage(riskLevel)
            );

            // 3. 알림 요청 생성
            BulkNotificationRequest request = BulkNotificationRequest.builder()
                    .userIds(favoriteUserIds)
                    .type("fire_risk")
                    .mountainId(mountain.getId())
                    .mountainName(mountain.getName())
                    .content(alertContent)
                    .title("🔥 산불 위험 알림")
                    .build();

            // 4. 알림 전송
            ResponseEntity<?> response = notificationServiceClient.sendBulkNotification(request);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("🔥 산불 알림 전송 성공: {} → {}명", mountain.getName(), favoriteUserIds.size());
            } else {
                log.error("❌ 산불 알림 전송 실패: {} → 응답코드 {}", mountain.getName(), response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 산별 알림 전송 실패: mountainId={}, mountainName={}",
                    mountain.getId(), mountain.getName(), e);
            throw e;
        }
    }

    /**
     * 위험도별 간단한 메시지
     */
    private String getSimpleRiskMessage(String riskLevel) {
        switch (riskLevel) {
            case "경보":
                return "등산을 자제하고 화기 사용을 금지해주세요.";
            case "주의":
                return "등산 시 화기 사용에 각별히 주의해주세요.";
            default:
                return "등산 계획을 재검토해주세요.";
        }
    }

    // ========== 기존 메서드들 (변경 없음) ==========

    private Map<String, Object> callFireRiskApi() {
        // API 키와 URL 검증
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("${fire.api.key}")) {
            log.error("❌ 산불 API 키가 설정되지 않았습니다. application-keys.yml 파일을 확인해주세요.");
            return createErrorResponse("API 키가 설정되지 않았습니다");
        }
        
        if (apiUrl == null || apiUrl.isEmpty() || apiUrl.equals("${fire.api.url}")) {
            log.error("❌ 산불 API URL이 설정되지 않았습니다. application-keys.yml 파일을 확인해주세요.");
            return createErrorResponse("API URL이 설정되지 않았습니다");
        }

        String url = apiUrl +
                "?serviceKey=" + apiKey +
                "&pageNo=1" +
                "&numOfRows=10" +
                "&_type=json";

        log.info("🔥 산림청 산불 API 호출 시작: {}", url.replace(apiKey, "***"));

        try {
            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                log.error("❌ API 응답이 null입니다");
                return createErrorResponse("API 응답이 null입니다");
            }

            log.info("✅ 산림청 API 호출 성공: 응답 키들 = {}", response.keySet());
            return response;

        } catch (Exception e) {
            log.error("❌ 산림청 API 호출 실패: {}", e.getMessage(), e);
            return createErrorResponse("API 호출 실패: " + e.getMessage());
        }
    }

    private Map<String, Object> parseFireRiskResponse(Map<String, Object> apiResponse) {
        try {
            log.info("🔍 산불 API 응답 파싱 시작: 전체 응답 = {}", apiResponse);
            
            Map<String, Object> response = (Map<String, Object>) apiResponse.get("response");
            if (response == null) {
                log.error("❌ response 키가 없습니다. 전체 응답: {}", apiResponse);
                return createErrorResponse("response 키가 없습니다");
            }

            Map<String, Object> body = (Map<String, Object>) response.get("body");
            if (body == null) {
                log.error("❌ body 키가 없습니다. response: {}", response);
                return createErrorResponse("body 키가 없습니다");
            }

            Map<String, Object> items = (Map<String, Object>) body.get("items");
            if (items == null) {
                log.error("❌ items 키가 없습니다. body: {}", body);
                return createErrorResponse("items 키가 없습니다");
            }

            Map<String, Object> item = (Map<String, Object>) items.get("item");
            if (item == null) {
                log.error("❌ item 키가 없습니다. items: {}", items);
                return createErrorResponse("산불위험예보 데이터가 없습니다");
            }

            log.info("✅ 파싱할 item 데이터: {}", item);

            Object meanAvgObj = item.get("meanavg");
            if (meanAvgObj == null) {
                log.error("❌ meanavg 키가 없습니다. item: {}", item);
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
            result.put("analdate", item.get("analdate"));
            result.put("date", LocalDate.now().toString());
            result.put("success", true);

            log.info("✅ 산불위험예보 파싱 완료: meanAvg={}, riskLevel={}", meanAvg, riskLevel);
            return result;

        } catch (Exception e) {
            log.error("❌ 산불위험예보 응답 파싱 실패: {}", e.getMessage(), e);
            return createErrorResponse("산불위험예보 데이터 파싱 실패: " + e.getMessage());
        }
    }

    private String calculateRiskLevel(int meanAvg) {
        if (meanAvg < 30) {
            return "안전";
        } else if (meanAvg <= 50) {
            return "주의";
        } else {
            return "경보";
        }
    }

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

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", message);
        return errorResponse;
    }
}