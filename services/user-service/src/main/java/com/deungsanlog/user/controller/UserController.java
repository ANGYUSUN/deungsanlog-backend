package com.deungsanlog.user.controller;


import com.deungsanlog.user.dto.UserCreateRequest;
import com.deungsanlog.user.dto.UserResponse;
import com.deungsanlog.user.entity.User;
import com.deungsanlog.user.repository.UserRepository;
import com.deungsanlog.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * 서비스 상태 확인 (Gateway에서 호출)
     * OAuth 보호 기능에서 제외해야 함 (Security 설정 필요)
     */
    @GetMapping("/status")
    public Map<String, String> getStatus() {
        return Map.of("message", "user-service is up!");
    }

    /**
     * OAuth2 사용자 저장/업데이트 (Gateway에서 호출하는 메인 엔드포인트)
     */
    @PostMapping("/oauth")
    public ResponseEntity<UserResponse> saveOrUpdateOAuthUser(@RequestBody UserCreateRequest request) {
        log.info("OAuth 사용자 저장/업데이트 요청: email={}, provider={}",
                request.getEmail(), request.getProvider());

        try {
            UserResponse response = userService.saveOrUpdateOAuthUser(request);
            log.info("OAuth 사용자 처리 완료: userId={}, email={}", response.getId(), response.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("OAuth 사용자 처리 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자 ID로 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        log.info("사용자 조회 요청: userId={}", userId);

        try {
            UserResponse response = userService.getUserById(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("사용자 조회 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("사용자 조회 중 오류: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 이메일로 사용자 조회
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.info("이메일로 사용자 조회 요청: email={}", email);

        try {
            UserResponse response = userService.getUserByEmail(email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("이메일로 사용자 조회 실패: email={}, error={}", email, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("이메일로 사용자 조회 중 오류: email={}, error={}", email, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자 목록 조회 (관리자용 - 선택사항)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("사용자 목록 조회: page={}, size={}", page, size);

        // TODO: 페이징 처리된 사용자 목록 조회 구현
        Map<String, Object> response = Map.of(
                "message", "사용자 목록 조회 기능은 추후 구현 예정",
                "page", page,
                "size", size
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/nickname")
    public ResponseEntity<String> getNickname(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        return ResponseEntity.ok(user.getNickname());
    }
}