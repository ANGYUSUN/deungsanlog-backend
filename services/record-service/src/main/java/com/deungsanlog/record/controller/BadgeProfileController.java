package com.deungsanlog.record.controller;

import com.deungsanlog.record.dto.BadgeProfileDto;
import com.deungsanlog.record.service.BadgeProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/records/users")
@RequiredArgsConstructor
public class BadgeProfileController {

    private final BadgeProfileService badgeProfileService;

    @GetMapping("/{userId}/badge-profile")
    public ResponseEntity<BadgeProfileDto> getBadgeProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(badgeProfileService.getBadgeProfile(userId));
    }
}
