package com.naebom.stroke.naebom.config;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class SpeechConfig {

    private static final String CREDENTIALS_PATH = "/Users/heosejin222gmail.com/Desktop/save/src/main/resources/google-cloud-key.json";

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        File credentialsFile = new File(CREDENTIALS_PATH);

        if (!credentialsFile.exists()) {
            throw new IOException("Google Cloud 인증 키 파일이 존재하지 않습니다: " + CREDENTIALS_PATH);
        }

        System.out.println("Google Cloud 인증 키 로드 성공! 경로: " + CREDENTIALS_PATH);
        return GoogleCredentials.fromStream(new FileInputStream(credentialsFile));
    }
}
