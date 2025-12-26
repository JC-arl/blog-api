package com.wsd.blogapi.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Profile("!test") // 테스트 환경에서는 실행하지 않음
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-path}")
    private String serviceAccountPath;

    @Value("${firebase.project-id}")
    private String projectId;

    @PostConstruct
    public void initialize() {
        try {
            File serviceAccountFile = new File(serviceAccountPath);

            if (!serviceAccountFile.exists()) {
                logger.warn("Firebase 서비스 계정 파일을 찾을 수 없습니다: {}", serviceAccountPath);
                logger.warn("Firebase 기능이 비활성화됩니다.");
                return;
            }

            FileInputStream serviceAccount = new FileInputStream(serviceAccountFile);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase 초기화 완료: {}", projectId);
            }
        } catch (IOException e) {
            logger.error("Firebase 초기화 실패: {}", e.getMessage());
            logger.warn("Firebase 기능이 비활성화됩니다.");
        }
    }
}
