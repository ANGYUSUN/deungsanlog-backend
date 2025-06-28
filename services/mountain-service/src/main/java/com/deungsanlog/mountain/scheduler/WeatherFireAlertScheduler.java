package com.deungsanlog.mountain.scheduler;

import com.deungsanlog.mountain.entity.Mountain;
import com.deungsanlog.mountain.repository.MountainRepository;
import com.deungsanlog.mountain.service.FireRiskApiService;
import com.deungsanlog.mountain.service.WeatherApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherFireAlertScheduler {

    private final FireRiskApiService fireRiskApiService;
    private final WeatherApiService weatherApiService;
    private final MountainRepository mountainRepository;

    /**
     * 🔔 매시간 정각에 모든 산의 산불/날씨 상황 체크 및 알림 전송
     */
    @Scheduled(cron = "0 0 */1 * * *") // 매시간 정각
    public void sendHourlyWeatherAndFireAlerts() {
        log.info("⏰ 정시 산불/날씨 알림 체크 시작");

        try {
            // 🏔️ 모든 산 조회
            List<Mountain> allMountains = mountainRepository.findAll();
            log.info("📍 총 {}개 산에 대해 알림 체크", allMountains.size());

            int fireAlertCount = 0;
            int weatherAlertCount = 0;

            for (Mountain mountain : allMountains) {
                try {
                    // 🔥 산불 위험도 체크 (기존 메서드 그대로 사용!)
                    fireRiskApiService.getFireRiskInfo(mountain.getLocation());
                    fireAlertCount++;

                    // 🌧️ 날씨 상황 체크 (기존 메서드 그대로 사용!)
                    if (mountain.getLatitude() != null && mountain.getLongitude() != null) {
                        weatherApiService.getCurrentWeather(mountain.getLongitude(), mountain.getLatitude());
                        weatherAlertCount++;
                    }

                    // API 호출 간격 조절 (과부하 방지)
                    Thread.sleep(500); // 0.5초 대기

                } catch (Exception e) {
                    log.error("❌ 산별 알림 체크 실패: {}", mountain.getName(), e);
                }
            }

            log.info("✅ 정시 알림 체크 완료: 산불체크 {}개, 날씨체크 {}개", fireAlertCount, weatherAlertCount);

        } catch (Exception e) {
            log.error("❌ 정시 알림 체크 전체 실패", e);
        }
    }
}