package com.deungsanlog.mountain.controller;

import com.deungsanlog.mountain.dto.MountainDetailDto;
import com.deungsanlog.mountain.dto.MountainRecordSearchResponse;
import com.deungsanlog.mountain.entity.Mountain;
import com.deungsanlog.mountain.service.MountainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mountains")
public class MountainController {

    @Autowired
    private MountainService mountainService;

    // ========== 기존 API들 ==========

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("message", "mountain-service is up!");
    }

    @GetMapping("/search")
    public MountainDetailDto searchMountain(@RequestParam String name) {
        return mountainService.searchMountain(name);
    }

    @GetMapping("/record/search")
    public List<MountainRecordSearchResponse> search(@RequestParam String keyword) {
        return mountainService.searchByKeyword(keyword);
    }

    @GetMapping("/name-by-id")
    public Map<String, String> getMountainNameById(@RequestParam Long mountainId) {
        String name = mountainService.getMountainBasic(mountainId).getName();
        return Map.of("name", name);
    }

    // ========== 🗺️ 지도용 새로운 API들 ==========

    /**
     * 카카오 지도에 마커 표시용 - 전체 산 목록 조회
     * 용도: 지도 로드 시 모든 산의 마커를 생성하기 위해
     */
    @GetMapping("/all")
    public List<Mountain> getAllMountainsForMap() {
        return mountainService.getAllMountainsForMap();
    }

    /**
     * 지도 마커 클릭 시 - 특정 산 정보 조회
     * 용도: 마커 클릭 시 팝업에 표시할 산 정보
     */
    @GetMapping("/{mountainId}")
    public Mountain getMountainById(@PathVariable Long mountainId) {
        return mountainService.getMountainBasic(mountainId);
    }

   
}