package com.deungsanlog.user.controller;

import com.deungsanlog.user.entity.MountainFavorite;
import com.deungsanlog.user.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 즐겨찾기 추가
     * POST /api/users/{userId}/favorites/{mountainId}
     */
    @PostMapping("/{userId}/favorites/{mountainId}")
    public ResponseEntity<Map<String, Object>> addFavorite(
            @PathVariable Long userId,
            @PathVariable Long mountainId) {

        log.info("즐겨찾기 추가 API 호출: userId={}, mountainId={}", userId, mountainId);

        try {
            boolean success = favoriteService.addFavorite(userId, mountainId);

            if (success) {
                Map<String, Object> response = Map.of(
                        "success", true,
                        "message", "즐겨찾기에 추가되었습니다.",
                        "userId", userId,
                        "mountainId", mountainId
                );
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = Map.of(
                        "success", false,
                        "message", "이미 즐겨찾기에 추가된 산입니다.",
                        "userId", userId,
                        "mountainId", mountainId
                );
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("즐겨찾기 추가 실패: userId={}, mountainId={}", userId, mountainId, e);
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "즐겨찾기 추가에 실패했습니다.",
                    "error", e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 즐겨찾기 삭제
     * DELETE /api/users/{userId}/favorites/{mountainId}
     */
    @DeleteMapping("/{userId}/favorites/{mountainId}")
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @PathVariable Long userId,
            @PathVariable Long mountainId) {

        log.info("즐겨찾기 삭제 API 호출: userId={}, mountainId={}", userId, mountainId);

        try {
            boolean success = favoriteService.removeFavorite(userId, mountainId);

            if (success) {
                Map<String, Object> response = Map.of(
                        "success", true,
                        "message", "즐겨찾기에서 삭제되었습니다.",
                        "userId", userId,
                        "mountainId", mountainId
                );
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = Map.of(
                        "success", false,
                        "message", "즐겨찾기에서 찾을 수 없습니다.",
                        "userId", userId,
                        "mountainId", mountainId
                );
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("즐겨찾기 삭제 실패: userId={}, mountainId={}", userId, mountainId, e);
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "즐겨찾기 삭제에 실패했습니다.",
                    "error", e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 즐겨찾기 토글 (있으면 삭제, 없으면 추가)
     * POST /api/users/{userId}/favorites/{mountainId}/toggle
     */
    @PostMapping("/{userId}/favorites/{mountainId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable Long userId,
            @PathVariable Long mountainId) {

        log.info("즐겨찾기 토글 API 호출: userId={}, mountainId={}", userId, mountainId);

        try {
            boolean isAdded = favoriteService.toggleFavorite(userId, mountainId);

            String message = isAdded ? "즐겨찾기에 추가되었습니다." : "즐겨찾기에서 삭제되었습니다.";

            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", message,
                    "isAdded", isAdded,
                    "userId", userId,
                    "mountainId", mountainId
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("즐겨찾기 토글 실패: userId={}, mountainId={}", userId, mountainId, e);
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "즐겨찾기 토글에 실패했습니다.",
                    "error", e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 즐겨찾기 여부 확인
     * GET /api/users/{userId}/favorites/{mountainId}/check
     */
    @GetMapping("/{userId}/favorites/{mountainId}/check")
    public ResponseEntity<Map<String, Object>> checkFavorite(
            @PathVariable Long userId,
            @PathVariable Long mountainId) {

        log.debug("즐겨찾기 여부 확인 API 호출: userId={}, mountainId={}", userId, mountainId);

        try {
            boolean isFavorite = favoriteService.isFavorite(userId, mountainId);

            Map<String, Object> response = Map.of(
                    "success", true,
                    "isFavorite", isFavorite,
                    "userId", userId,
                    "mountainId", mountainId
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("즐겨찾기 확인 실패: userId={}, mountainId={}", userId, mountainId, e);
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "즐겨찾기 확인에 실패했습니다.",
                    "error", e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 사용자의 즐겨찾기 산 ID 목록 조회
     * GET /api/users/{userId}/favorites/ids
     */
    @GetMapping("/{userId}/favorites/ids")
    public ResponseEntity<Map<String, Object>> getFavoriteIds(@PathVariable Long userId) {
        log.info("즐겨찾기 ID 목록 조회 API 호출: userId={}", userId);

        try {
            List<Long> favoriteIds = favoriteService.getFavoriteIds(userId);

            Map<String, Object> response = Map.of(
                    "success", true,
                    "favoriteIds", favoriteIds,
                    "count", favoriteIds.size(),
                    "userId", userId
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("즐겨찾기 ID 목록 조회 실패: userId={}", userId, e);
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "즐겨찾기 목록 조회에 실패했습니다.",
                    "error", e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 사용자의 즐겨찾기 개수 조회
     * GET /api/users/{userId}/favorites/count
     */
    @GetMapping("/{userId}/favorites/count")
    public ResponseEntity<Map<String, Object>> getFavoriteCount(@PathVariable Long userId) {
        log.info("즐겨찾기 개수 조회 API 호출: userId={}", userId);

        try {
            int count = favoriteService.getFavoriteCount(userId);

            Map<String, Object> response = Map.of(
                    "success", true,
                    "count", count,
                    "userId", userId
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("즐겨찾기 개수 조회 실패: userId={}", userId, e);
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "즐겨찾기 개수 조회에 실패했습니다.",
                    "error", e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 사용자의 즐겨찾기 전체 목록 조회 (관리용)
     * GET /api/users/{userId}/favorites
     */
    @GetMapping("/{userId}/favorites")
    public ResponseEntity<Map<String, Object>> getUserFavorites(@PathVariable Long userId) {
        log.info("즐겨찾기 전체 목록 조회 API 호출: userId={}", userId);

        try {
            List<MountainFavorite> favorites = favoriteService.getUserFavorites(userId);

            Map<String, Object> response = Map.of(
                    "success", true,
                    "favorites", favorites,
                    "count", favorites.size(),
                    "userId", userId
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("즐겨찾기 전체 목록 조회 실패: userId={}", userId, e);
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "즐겨찾기 목록 조회에 실패했습니다.",
                    "error", e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 특정 산의 즐겨찾기 사용자 수 조회
     * GET /api/users/mountains/{mountainId}/favorites/count
     */
    @GetMapping("/mountains/{mountainId}/favorites/count")
    public ResponseEntity<Map<String, Object>> getMountainFavoriteCount(@PathVariable Long mountainId) {
        log.info("산 즐겨찾기 사용자 수 조회 API 호출: mountainId={}", mountainId);

        try {
            int count = favoriteService.getMountainFavoriteCount(mountainId);

            Map<String, Object> response = Map.of(
                    "success", true,
                    "count", count,
                    "mountainId", mountainId
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("산 즐겨찾기 사용자 수 조회 실패: mountainId={}", mountainId, e);
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "즐겨찾기 사용자 수 조회에 실패했습니다.",
                    "error", e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ========== 🆕 알림 서비스용 API 추가 ==========

    /**
     * 특정 산을 즐겨찾기한 사용자 ID 목록 조회 (Notification Service에서 호출)
     * GET /api/users/mountains/{mountainId}/favorite-users
     */

    
}