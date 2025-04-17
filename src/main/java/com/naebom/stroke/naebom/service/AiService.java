package com.naebom.stroke.naebom.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class AiService {
    private final RestTemplate restTemplate;

    public AiService() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter()); // JSON 변환기 추가
    }

    public Double sendImageToAi(MultipartFile image) throws IOException {
        String aiUrl = "http://192.168.9.160:5000/api/ai_send"; // Flask AI 서버 주소

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");

        // ✅ JPEG 이미지를 그대로 전송
        ByteArrayResource fileAsResource = new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        };

        // ✅ form-data 형식 요청 생성
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", fileAsResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // ✅ Flask AI 서버로 이미지 전송 및 응답 받기
        ResponseEntity<Map> response = restTemplate.exchange(aiUrl, HttpMethod.POST, requestEntity, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null && responseBody.containsKey("score")) {
            return ((Number) responseBody.get("score")).doubleValue(); // AI 점수를 Double로 변환
        }

        return null; // 점수가 없는 경우 null 반환
    }
}
