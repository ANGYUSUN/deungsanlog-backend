package com.deungsanlog.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.config-path}")
    private Resource firebaseConfigPath;

    @Value("${firebase.project.id}")
    private String projectId;

    @PostConstruct
    public void initializeFirebase() {
        try {
            log.info("🔥 Firebase 초기화 시작: projectId={}", projectId);
            log.info("🔥 Firebase 설정 파일 경로: {}", firebaseConfigPath.getDescription());

            // Firebase가 이미 초기화되어 있는지 확인
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = firebaseConfigPath.getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(projectId)
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase 초기화 완료!");
            } else {
                log.info("🔥 Firebase가 이미 초기화되어 있습니다.");
            }
        } catch (IOException e) {
            log.error("❌ Firebase 초기화 실패", e);
            throw new RuntimeException("Firebase 초기화 실패: " + e.getMessage(), e);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        try {
            FirebaseMessaging messaging = FirebaseMessaging.getInstance();
            log.info("✅ FirebaseMessaging Bean 생성 완료");
            return messaging;
        } catch (Exception e) {
            log.error("❌ FirebaseMessaging Bean 생성 실패", e);
            throw new RuntimeException("FirebaseMessaging Bean 생성 실패: " + e.getMessage(), e);
        }
    }
}