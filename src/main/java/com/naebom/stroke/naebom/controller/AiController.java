package com.naebom.stroke.naebom.controller;

import com.naebom.stroke.naebom.service.AiService;
import com.naebom.stroke.naebom.service.TestRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;
    private final TestRecordService testRecordService;

    public AiController(AiService aiService, TestRecordService testRecordService) {
        this.aiService = aiService;
        this.testRecordService = testRecordService;
    }

    @PostMapping("/upload/{memberId}") // ✅ memberId를 URL 파라미터로 변경
    public ResponseEntity<Map<String, Object>> uploadImageToAi(
            @PathVariable Long memberId,  // ✅ URL에서 memberId 받음
            @RequestParam("image") MultipartFile image) {
        try {
            if (image.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No image uploaded"));
            }

            // ✅ AI 서버로 이미지 전송 후 점수 받기
            Double aiScore = aiService.sendImageToAi(image);

            if (aiScore == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to retrieve score from AI"));
            }



            return ResponseEntity.ok(Map.of("AI Score", aiScore));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Image processing error: " + e.getMessage()));
        }
    }
}
