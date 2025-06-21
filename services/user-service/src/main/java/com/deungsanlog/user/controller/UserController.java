package com.deungsanlog.user.controller;

import com.deungsanlog.user.dto.UserCreateRequest;
import com.deungsanlog.user.dto.UserResponse;
import com.deungsanlog.user.dto.UserUpdateRequest;
import com.deungsanlog.user.entity.User;
import com.deungsanlog.user.repository.UserRepository;
import com.deungsanlog.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * 서비스 상태 확인 (Gateway에서 호출)
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
     * 🆕 프로필 수정 API
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request) {

        log.info("프로필 수정 요청: userId={}, nickname={}", userId, request.getNickname());

        try {
            UserResponse response = userService.updateUser(userId, request);
            log.info("프로필 수정 완료: userId={}, nickname={}", userId, response.getNickname());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("프로필 수정 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("프로필 수정 중 오류: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🛠️ 프로필 이미지 업로드 API (경로 수정 완료!)
     */
    @PostMapping("/upload-profile-image")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {

        log.info("프로필 이미지 업로드 요청: userId={}, fileName={}", userId, file.getOriginalFilename());

        try {
            // 파일 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "파일이 비어있습니다."));
            }

            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "이미지 파일만 업로드 가능합니다."));
            }

            if (file.getSize() > 5 * 1024 * 1024) { // 5MB 제한
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "파일 크기는 5MB 이하로 업로드해주세요."));
            }

            // 🔧 파일 저장 경로 수정 (실제 폴더 구조에 맞춤)
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String uploadDir = System.getProperty("user.dir") + "/services/user-service/uploads/profiles";// ✅ 경로 수정!
            Path uploadPath = Paths.get(uploadDir);

            // 디렉토리 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("📁 디렉토리 생성: {}", uploadPath.toAbsolutePath());
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // 🔧 이미지 URL 수정 (User Service 직접 접근)
            String imageUrl = "http://localhost:8081/uploads/profiles/" + fileName; // ✅ URL 수정!

            log.info("✅ 프로필 이미지 업로드 완료: userId={}, imageUrl={}", userId, imageUrl);
            log.info("📁 실제 저장 경로: {}", filePath.toAbsolutePath());

            return ResponseEntity.ok(Map.of(
                    "success", "true",
                    "imageUrl", imageUrl,
                    "message", "이미지 업로드 성공"
            ));

        } catch (IOException e) {
            log.error("❌ 프로필 이미지 업로드 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "파일 업로드 중 오류가 발생했습니다."));
        } catch (Exception e) {
            log.error("❌ 프로필 이미지 업로드 중 예외: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
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

        Map<String, Object> response = Map.of(
                "message", "사용자 목록 조회 기능은 추후 구현 예정",
                "page", page,
                "size", size
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 닉네임 조회 (Record Service에서 사용)
     */
    @GetMapping("/{id}/nickname")
    public ResponseEntity<String> getNickname(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        return ResponseEntity.ok(user.getNickname());
    }
}