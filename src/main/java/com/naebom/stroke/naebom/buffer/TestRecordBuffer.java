package com.naebom.stroke.naebom.buffer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TestRecordBuffer {

    private final Map<Long, PartialTestRecord> buffer = new ConcurrentHashMap<>();

    public void updateFaceScore(Long memberId, Double score) {
        buffer.computeIfAbsent(memberId, id -> new PartialTestRecord()).setFaceTestScore(score);
        log.info("  얼굴 점수: {}", buffer.get(memberId).getFaceTestScore());
    }

    public void updateSpeechScore(Long memberId, Double score) {
        buffer.computeIfAbsent(memberId, id -> new PartialTestRecord()).setSpeechTestScore(score);
        log.info("  발음 점수: {}", buffer.get(memberId).getSpeechTestScore());
    }

    public void updateFingerScore(Long memberId, Double score) {
        buffer.computeIfAbsent(memberId, id -> new PartialTestRecord()).setFingerTestScore(score);
        log.info("  손가락 점수: {}", buffer.get(memberId).getFingerTestScore());
    }

    public void updateArmScore(Long memberId, Double score) {
        buffer.computeIfAbsent(memberId, id -> new PartialTestRecord()).setArmTestScore(score);
        log.info("  팔 점수: {}", buffer.get(memberId).getArmTestScore());
    }

    public void updateFeedback(Long memberId, String feedback) {
        log.info("[피드백 저장 요청] memberId: {}, feedback: {}", memberId, feedback);
        PartialTestRecord record = buffer.get(memberId);
        if (record != null) {
            record.setFeedback(feedback);
            log.info("  피드백 점수: {}", buffer.get(memberId).getFeedback());
        }
    }

    // 검사 점수 4개가 모두 채워졌는지 확인 (프론트에 응답용)
// TestRecordBuffer.java
    public boolean isComplete(Long memberId) {
        PartialTestRecord record = buffer.get(memberId);
        return record != null &&
                record.getFaceTestScore() != null &&
                record.getSpeechTestScore() != null &&
                record.getFingerTestScore() != null &&
                record.getArmTestScore() != null &&
                record.getFeedback() != null;
    }


    // 점수 4개만 완료됐을 때 꺼내기 (feedback은 없어도 됨)
    public PartialTestRecord getIfComplete(Long memberId) {
        if (isComplete(memberId)) {
            return buffer.get(memberId);
        }
        return null;
    }

    // 최종 저장 및 버퍼 제거용
    public PartialTestRecord getAndRemove(Long memberId) {
        return buffer.remove(memberId);
    }
    public boolean isAllScoresCompleted(Long memberId) {
        PartialTestRecord record = buffer.get(memberId);
        return record != null &&
                record.getFaceTestScore() != null &&
                record.getSpeechTestScore() != null &&
                record.getFingerTestScore() != null &&
                record.getArmTestScore() != null;
    }

    // 점수만 완료된 record 꺼내기 (프론트 응답용, 삭제 X)
    public PartialTestRecord getIfAllScoresCompleted(Long memberId) {
        if (isAllScoresCompleted(memberId)) {
            return buffer.get(memberId);
        }
        return null;
    }
    public boolean hasAllScores(Long memberId) {
        PartialTestRecord record = buffer.get(memberId);
        return record != null &&
                record.getFaceTestScore() != null &&
                record.getSpeechTestScore() != null &&
                record.getFingerTestScore() != null &&
                record.getArmTestScore() != null;

    }

    public PartialTestRecord get(Long memberId) {
        return buffer.get(memberId);
    }

}
