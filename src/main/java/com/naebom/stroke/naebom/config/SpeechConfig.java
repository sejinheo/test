package com.naebom.stroke.naebom.config;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class SpeechConfig {

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        // Qoddi 환경변수에서 JSON 문자열 읽기
        String credentialsJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON");

        if (credentialsJson == null || credentialsJson.isEmpty()) {
            throw new IOException("환경변수 GOOGLE_APPLICATION_CREDENTIALS_JSON이 설정되지 않았습니다.");
        }

        System.out.println("✅ Google Cloud 인증 환경변수 정상 로딩됨");
        return GoogleCredentials.fromStream(
                new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))
        );
    }
}
