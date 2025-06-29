package com.deungsanlog.record.service;

import com.deungsanlog.record.domain.RecordHiking;
import com.deungsanlog.record.dto.RecordHikingResponse;
import com.deungsanlog.record.repository.RecordHikingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Value("${record.upload-path}")
    private String uploadDir;

    public void create(Long userId, Long mountainId, String mountainName, LocalDate date, String content, MultipartFile photo) {
        System.out.println("📩 등산 기록 생성 요청 받음!");
        System.out.println("👤 userId: " + userId);
        System.out.println("⛰ mountainId: " + mountainId);
        System.out.println("⛰ mountainName: " + mountainName);
        System.out.println("📅 recordDate: " + date);
        System.out.println("📝 content: " + content);
        System.out.println("📷 photo.originalFilename: " + photo.getOriginalFilename());

        String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);

        System.out.println("📂 실제 파일 저장 경로: " + filePath.toAbsolutePath());

        try {
            Files.createDirectories(Paths.get(uploadDir));
            Files.copy(photo.getInputStream(), filePath);
            System.out.println("✅ 파일 저장 성공: " + filePath.toAbsolutePath());
            System.out.println("✅ 파일 존재 여부: " + Files.exists(filePath));
        } catch (IOException e) {
            throw new RuntimeException("사진 저장 실패", e);
        }

        RecordHiking record = RecordHiking.builder()
                .userId(userId)
                .mountainId(mountainId)
                .mountainName(mountainName)
                .recordDate(date)
                .content(content)
                .photoUrl("/uploads/" + fileName)
                .build();

        recordHikingRepository.save(record);
    }

    public RecordHikingResponse getRecordById(Long recordId) {
        RecordHiking record = recordHikingRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("해당 기록이 없습니다."));
        return RecordHikingResponse.from(record);
    }

    public Page<RecordHikingResponse> getRecordsByUser(Long userId, Pageable pageable) {
        return recordHikingRepository.findByUserId(userId, pageable)
                .map(RecordHikingResponse::from);
    }

    public void edit(Long recordId, String mountainName, LocalDate recordDate, String content, MultipartFile photo) {
        RecordHiking record = recordHikingRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("해당 기록이 없습니다."));

        if (mountainName != null) record.setMountainName(mountainName);
        if (recordDate != null) record.setRecordDate(recordDate);
        if (content != null) record.setContent(content);

        if (photo != null && !photo.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            try {
                Files.createDirectories(Paths.get(uploadDir));
                Files.copy(photo.getInputStream(), filePath);
                record.setPhotoUrl("/uploads/" + fileName);
            } catch (IOException e) {
                throw new RuntimeException("사진 저장 실패", e);
            }
        }

        recordHikingRepository.save(record);
    }

    public void delete(Long recordId) {
        RecordHiking record = recordHikingRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("해당 기록이 없습니다."));

        String photoUrl = record.getPhotoUrl();
        if (photoUrl != null && !photoUrl.isBlank()) {
            // /uploads/uuid_파일명 형태이므로 마지막 파일명만 추출
            String fileName = Paths.get(photoUrl).getFileName().toString();
            Path filePath = Paths.get(uploadDir, fileName);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("사진 파일 삭제 실패: " + filePath);
            }
        }

        recordHikingRepository.deleteById(recordId);
    }
}
