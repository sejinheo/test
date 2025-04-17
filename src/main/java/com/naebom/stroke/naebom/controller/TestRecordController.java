package com.naebom.stroke.naebom.controller;

import com.naebom.stroke.naebom.buffer.PartialTestRecord;
import com.naebom.stroke.naebom.buffer.TestRecordBuffer;
import com.naebom.stroke.naebom.dto.TestRecordDto;
import com.naebom.stroke.naebom.service.TestRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test-records")
@RequiredArgsConstructor
@Slf4j
public class TestRecordController {

    private final TestRecordService testRecordService;
    private final TestRecordBuffer testRecordBuffer;

    // 전체 검사 기록 조회
    @GetMapping("/history/{memberId}")
    public ResponseEntity<List<TestRecordDto>> getTestHistory(@PathVariable Long memberId) {
        return ResponseEntity.ok(testRecordService.getTestHistory(memberId));
    }

    // 최근 2개 검사 기록 조회
    @GetMapping("/recent/{memberId}")
    public ResponseEntity<List<TestRecordDto>> getRecentTwoTestRecords(@PathVariable Long memberId) {
        return ResponseEntity.ok(testRecordService.getRecentTwoTestRecords(memberId));
    }

    // 간단한 전체 검사 기록 조회 (null 제거)
    @GetMapping("/history-simple/{memberId}")
    public ResponseEntity<List<Map<String, Object>>> getSimpleTestHistory(@PathVariable Long memberId) {
        return ResponseEntity.ok(testRecordService.getSimpleTestHistory(memberId));
    }

    // 최근 2개 간단 검사 기록 조회 (null 제거)
   /* @GetMapping("/recent-simple/{memberId}")
    public ResponseEntity<List<Map<String, Object>>> getRecentTwoSimpleTestRecords(@PathVariable Long memberId) {
        return ResponseEntity.ok(testRecordService.getRecentTwoSimpleTestRecords(memberId));
    }*/
    @GetMapping("/recent-simple/{memberId}")
    public ResponseEntity<Map<String, Object>> getRecentTwoSimpleTestRecords(@PathVariable Long memberId) {
        List<Map<String, Object>> recentRecords = testRecordService.getRecentTwoSimpleTestRecords(memberId);
        long last30DaysCount = testRecordService.getTestCountLast30Days(memberId);

        Map<String, Object> response = new HashMap<>();
        response.put("recentRecords", recentRecords);
        response.put("last30DaysCount", last30DaysCount);

        return ResponseEntity.ok(response);
    }


    // 얼굴 점수 저장
    @PostMapping("/save-face-score")
    public ResponseEntity<?> saveFace(@RequestBody Map<String, Object> body) {
        Long memberId = Long.valueOf(body.get("memberId").toString());
        Double faceTestScore = Double.valueOf(body.get("faceTestScore").toString());
        testRecordBuffer.updateFaceScore(memberId, faceTestScore);

        if (testRecordBuffer.isAllScoresCompleted(memberId)) {
            PartialTestRecord record = testRecordBuffer.getIfAllScoresCompleted(memberId);
            return ResponseEntity.ok(record);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "face 저장 완료");
        return ResponseEntity.ok(response);
    }

    // 발음 점수 저장
    @PostMapping("/save-speech-score")
    public ResponseEntity<?> saveSpeech(@RequestBody Map<String, Object> body) {
        Long memberId = Long.valueOf(body.get("memberId").toString());
        Double speechTestScore = Double.valueOf(body.get("speechTestScore").toString());
        testRecordBuffer.updateSpeechScore(memberId, speechTestScore);

        if (testRecordBuffer.hasAllScores(memberId)) {
            PartialTestRecord record = testRecordBuffer.get(memberId);

            double avgScore = (
                    record.getFaceTestScore() +
                            record.getSpeechTestScore() +
                            record.getFingerTestScore() +
                            record.getArmTestScore()
            ) / 3.0;

            double avgRiskScore = Math.round((100 - avgScore) * 10.0) / 10.0;

            Map<String, Object> response = new HashMap<>();
            response.put("faceTestScore", record.getFaceTestScore());
            response.put("speechTestScore", record.getSpeechTestScore());
            response.put("fingerTestScore", record.getFingerTestScore());
            response.put("armTestScore", record.getArmTestScore());
            response.put("avgRiskScore", avgRiskScore);

            return ResponseEntity.ok(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Speech 저장 완료");
        return ResponseEntity.ok(response);
    }


    // 손가락 점수 저장
    @PostMapping("/save-finger-score")
    public ResponseEntity<?> saveFinger(@RequestBody Map<String, Object> body) {
        Long memberId = Long.valueOf(body.get("memberId").toString());
        Double fingerTestScore = Double.valueOf(body.get("fingerTestScore").toString());
        testRecordBuffer.updateFingerScore(memberId, fingerTestScore);

        if (testRecordBuffer.isAllScoresCompleted(memberId)) {
            PartialTestRecord record = testRecordBuffer.getIfAllScoresCompleted(memberId);
            return ResponseEntity.ok(record);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "finger 저장 완료");
        return ResponseEntity.ok(response);
    }

    // 팔 근육 점수 저장
    @PostMapping("/save-arm-score")
    public ResponseEntity<?> saveArm(@RequestBody Map<String, Object> body) {
        Long memberId = Long.valueOf(body.get("memberId").toString());
        Double armTestScore = Double.valueOf(body.get("armTestScore").toString());

        testRecordBuffer.updateArmScore(memberId, armTestScore);

        if (testRecordBuffer.isAllScoresCompleted(memberId)) {
            PartialTestRecord record = testRecordBuffer.getIfAllScoresCompleted(memberId);
            return ResponseEntity.ok(record);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "arm 저장 완료");
        return ResponseEntity.ok(response);
    }

    // 피드백 저장 (strokeRisk는 사용하지 않음)/////////////////////////////////////////
    @PostMapping("/save-feedback-risk")
    public ResponseEntity<?> saveFeedback(@RequestBody Map<String, Object> body) {
        log.info("body 들어온 값: {}", body);
        log.info("[컨트롤러] saveFeedback 호출됨");

        Long memberId = Long.valueOf(body.get("memberId").toString());
        String feedback = body.get("feedback").toString();
        testRecordService.saveFeedbackAndRisk(memberId, feedback);
       // return ResponseEntity.ok("피드백 저장 완료");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "피드백 저장 완료");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/detail")
    public ResponseEntity<TestRecordDto> getRecordByIdAndMember(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("id");
        Object memberIdObj = body.get("memberId");

        if (idObj == null || memberIdObj == null) {
            throw new IllegalArgumentException("id 또는 memberId가 요청에 없습니다.");
        }

        Long id = Long.valueOf(idObj.toString());
        Long memberId = Long.valueOf(memberIdObj.toString());

        return ResponseEntity.ok(testRecordService.getOneTestRecord(id, memberId));
    }
    @GetMapping("/buffer/{memberId}")
    public ResponseEntity<?> getBuffer(@PathVariable Long memberId) {
        PartialTestRecord record = testRecordBuffer.get(memberId);
        if (record == null) {
            return ResponseEntity.ok("해당 멤버의 버퍼에 데이터가 없습니다.");
        }
        return ResponseEntity.ok(record);
    }

    @GetMapping("/result/{memberId}")
    public ResponseEntity<?> getResult(@PathVariable Long memberId) {
        PartialTestRecord record = testRecordBuffer.getIfAllScoresCompleted(memberId);

        if (record == null) {
            return ResponseEntity.badRequest().body("아직 모든 점수가 입력되지 않았습니다.");
        }

        double avgScore = (
                record.getFaceTestScore() +
                        record.getSpeechTestScore() +
                        record.getFingerTestScore() +
                        record.getArmTestScore()
        ) / 3.0;

        double avgRiskScore = Math.round((100 - avgScore) * 10.0) / 10.0;

        Map<String, Object> result = new HashMap<>();
        result.put("faceTestScore", record.getFaceTestScore());
        result.put("speechTestScore", record.getSpeechTestScore());
        result.put("fingerTestScore", record.getFingerTestScore());
        result.put("armTestScore", record.getArmTestScore());
        result.put("avgRiskScore", avgRiskScore);

        return ResponseEntity.ok(result);
    }
}
