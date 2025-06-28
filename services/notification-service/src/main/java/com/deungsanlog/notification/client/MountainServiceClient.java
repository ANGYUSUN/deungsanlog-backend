package com.deungsanlog.notification.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "mountain-service")
public interface MountainServiceClient {
    // 추후 필요 시 산 정보나 즐겨찾기 사용자 조회용 메서드 추가 예정
}