package com.deungsanlog.record.service;

import com.deungsanlog.record.domain.RecordHiking;
import com.deungsanlog.record.repository.RecordHikingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordHikingService {

    private final RecordHikingRepository recordHikingRepository;

    public void create(Long userId, Long mountainId, LocalDate date, String content, MultipartFile photo) {
        System.out.println("📩 등산 기록 생성 요청 받음!");
        System.out.println("👤 userId: " + userId);
        System.out.println("⛰ mountainId: " + mountainId);
        System.out.println("📅 recordDate: " + date);
        System.out.println("📝 content: " + content);
        System.out.println("📷 photo.originalFilename: " + photo.getOriginalFilename());

        String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
        String uploadDir = System.getProperty("user.dir") + "/uploads";  // 절대 경로로 지정
        Path filePath = Paths.get(uploadDir, fileName);

        try {
            Files.createDirectories(Paths.get(uploadDir)); // 디렉토리가 없으면 생성
            Files.copy(photo.getInputStream(), filePath);
        } catch (IOException e) {
            throw new RuntimeException("사진 저장 실패", e);
        }


        // 🗃️ DB 저장
        RecordHiking record = RecordHiking.builder()
                .userId(userId)
                .mountainId(mountainId)
                .recordDate(date)
                .content(content)
                .photoUrl("/uploads/" + fileName) // static path
                .build();

        recordHikingRepository.save(record);
    }
}
