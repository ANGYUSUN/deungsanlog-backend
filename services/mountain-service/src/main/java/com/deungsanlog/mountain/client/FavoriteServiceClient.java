package com.deungsanlog.mountain.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 즐겨찾기 관련 User Service 호출용 Feign Client
 */
@FeignClient(name = "user-service")
public interface FavoriteServiceClient {

    /**
     * 특정 산을 즐겨찾기한 사용자 ID 목록 조회(특정 산 즐겨찾기 한 사람들에게 알림쏘기위해)
     *
     * @param mountainId 산 ID
     * @return 해당 산을 즐겨찾기한 사용자 ID 목록
     */
    @GetMapping("/api/users/mountains/{mountainId}/favorite-users")
    List<Long> getFavoriteUserIds(@PathVariable("mountainId") Long mountainId);
}