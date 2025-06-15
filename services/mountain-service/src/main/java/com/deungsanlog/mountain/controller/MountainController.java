package com.deungsanlog.mountain.controller;

import com.deungsanlog.mountain.dto.MountainDetailDto;
import com.deungsanlog.mountain.dto.MountainRecordSearchResponse;
import com.deungsanlog.mountain.service.MountainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

//http 요청 처리,autowired 쓰기위해 빈으로 등록
@RestController
@RequestMapping("/api/mountains")
public class MountainController {

    //자동연결(객체를 찾을려고 spring에서 컨테이너 뒤져봄)
    @Autowired
    private MountainService mountainService; // Repository 대신 Service 주입

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("message", "mountain-service is up!");
    }

    @GetMapping("/search")
    public MountainDetailDto searchMountain(@RequestParam String name) {
        return mountainService.searchMountain(name); // Service 호출
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
}