package com.naebom.stroke.naebom.service;

import com.naebom.stroke.naebom.buffer.PartialTestRecord;
import com.naebom.stroke.naebom.buffer.TestRecordBuffer;
import com.naebom.stroke.naebom.dto.TestRecordDto;
import com.naebom.stroke.naebom.entity.Member;
import com.naebom.stroke.naebom.entity.TestRecord;
import com.naebom.stroke.naebom.repository.MemberRepository;
import com.naebom.stroke.naebom.repository.TestRecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class TestRecordService {

    private final TestRecordRepository testRecordRepository;
    private final MemberRepository memberRepository;
    private final TestRecordBuffer testRecordBuffer;

    public List<TestRecordDto> getTestHistory(Long memberId) {
        List<TestRecord> records = testRecordRepository.findByMemberIdOrderByTestDateDesc(memberId);
        return convertToDto(records);
    }

    public List<TestRecordDto> getRecentTwoTestRecords(Long memberId) {
        List<TestRecord> records = testRecordRepository.findRecentTestRecords(memberId);
        return convertToDto(records);
    }

    private List<TestRecordDto> convertToDto(List<TestRecord> records) {
        return records.stream()
                .map(record -> new TestRecordDto(
                        record.getId(),
                        record.getMember().getId(),
                        record.getTestDate(),
                        record.getFaceTestScore(),
                        record.getSpeechTestScore(),
                        record.getFingerTestScore(),
                        record.getArmTestScore(),
                        record.getStrokeRisk(),
                        record.getTestCount(),
                        record.getFeedback(),
                        record.getAvgRiskScore(),
                        null

                ))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getRecentTwoSimpleTestRecords(Long memberId) {
        List<TestRecord> records = testRecordRepository.findTop2ByMemberIdOrderByIdDesc(memberId);
        return convertToSimpleDto(records);
    }

    public List<Map<String, Object>> getSimpleTestHistory(Long memberId) {
        List<TestRecord> records = testRecordRepository.findByMemberIdOrderByTestDateDesc(memberId);
        return convertToSimpleDto(records);
    }

    private List<Map<String, Object>> convertToSimpleDto(List<TestRecord> records) {
        return records.stream()
                .map(record -> {
                    Map<String, Object> result = new HashMap<>();

                    if (record.getMember() != null && record.getMember().getId() != null) {
                        result.put("memberId", record.getMember().getId());
                    } else {
                        result.put("memberId", null);
                    }

                    if (record.getTestDate() != null) {
                        result.put("testDate", record.getTestDate());
                    } else {
                        result.put("testDate", null);
                    }

                    if (record.getAvgRiskScore() != null) {
                        result.put("avgRiskScore", Math.round(record.getAvgRiskScore() * 10.0) / 10.0);
                    } else {
                        result.put("avgRiskScore", null);
                    }

                    if (record.getFeedback() != null) {
                        result.put("feedback", record.getFeedback());
                    }

                    if (record.getTestCount() != null) {
                        result.put("testCount", record.getTestCount());
                    }

                    return result;
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public void saveFaceTestScore(Long memberId, Double score) {
        testRecordBuffer.updateFaceScore(memberId, score);
        checkAndSaveIfComplete(memberId);
    }

    @Transactional
    public void saveFingerTestScore(Long memberId, Double score) {
        testRecordBuffer.updateFingerScore(memberId, score);
        checkAndSaveIfComplete(memberId);
    }

    @Transactional
    public void saveArmTestScore(Long memberId, Double score) {
        testRecordBuffer.updateArmScore(memberId, score);
        checkAndSaveIfComplete(memberId);
    }

    @Transactional
    public void saveSpeechTestScore(Long memberId, Double score) {
        testRecordBuffer.updateSpeechScore(memberId, score);
        checkAndSaveIfComplete(memberId);
    }

    @Transactional
    public void saveFeedbackAndRisk(Long memberId, String feedback) {
        log.info("[서비스] saveFeedbackAndRisk() 호출됨 - memberId: {}, feedback: {}", memberId, feedback);

        testRecordBuffer.updateFeedback(memberId, feedback);
        checkAndSaveIfComplete(memberId);
    }
    public TestRecordDto getOneTestRecord(Long id, Long memberId) {
        TestRecord record = testRecordRepository.findByIdAndMember_Id(id, memberId)
                .orElseThrow(() -> new RuntimeException("검사 기록이 존재하지 않습니다."));
        Optional<TestRecord> prevRecord = testRecordRepository.findByMember_IdAndTestCount(
                memberId,
                record.getTestCount() - 1
        );
        return TestRecordDto.builder()
                .id(record.getId())
                .memberId(record.getMember().getId())
                .testDate(record.getTestDate())
                .faceTestScore(record.getFaceTestScore())
                .speechTestScore(round1(record.getSpeechTestScore()))
                .fingerTestScore(record.getFingerTestScore())
                .armTestScore(record.getArmTestScore())
                .strokeRisk(record.getStrokeRisk())
                .testCount(record.getTestCount())
                .feedback(record.getFeedback())
                .avgRiskScore(record.getAvgRiskScore())
                .prevAvgRiskScore(prevRecord.map(TestRecord::getAvgRiskScore).orElse(null))
                .build();
    }
    private Double round1(Double value) {
        return value == null ? null : Math.round(value * 10.0) / 10.0;
    }
    public void saveAllScoresAndFeedback(Long memberId, PartialTestRecord record) {
        saveFaceTestScore(memberId, record.getFaceTestScore());
        saveSpeechTestScore(memberId, record.getSpeechTestScore());
        saveFingerTestScore(memberId, record.getFingerTestScore());
        saveArmTestScore(memberId, record.getArmTestScore());
        saveFeedbackAndRisk(memberId, record.getFeedback());
    }
    public PartialTestRecord getBufferRecordIfAllScores(Long memberId) {
        if (testRecordBuffer.hasAllScores(memberId)) {
            return testRecordBuffer.get(memberId);
        }
        return null;
    }
    public long getTestCountLast30Days(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(30);
        return testRecordRepository.countByMemberIdInLast30Days(memberId, startDate);
    }

    private void checkAndSaveIfComplete(Long memberId) {
        if (testRecordBuffer.isComplete(memberId)) {
            PartialTestRecord buffer = testRecordBuffer.get(memberId);

            log.info("[검사 완료] 모든 검사 항목이 입력됨 - memberId: {}", memberId);
            log.info("→ 얼굴 점수: {}", buffer.getFaceTestScore());
            log.info("→ 발음 점수: {}", buffer.getSpeechTestScore());
            log.info("→ 손가락 점수: {}", buffer.getFingerTestScore());
            log.info("→ 팔 점수: {}", buffer.getArmTestScore());
            log.info("→ 피드백: {}", buffer.getFeedback());

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));

            int testCount = testRecordRepository.countByMemberId(memberId) + 1;

            double avgScore = (buffer.getFaceTestScore() + buffer.getSpeechTestScore()
                    + buffer.getFingerTestScore() + buffer.getArmTestScore()) / 3.0;
            double avgRiskScore = Math.round((100 - avgScore) * 10.0) / 10.0;

            log.info("→ 평균 점수 계산: {}, 평균 위험 점수: {}", avgScore, avgRiskScore);

            TestRecord newRecord = TestRecord.builder()
                    .member(member)
                    .testDate(LocalDate.now())
                    .testCount(testCount)
                    .strokeRisk(false)
                    .faceTestScore(buffer.getFaceTestScore())
                    .speechTestScore(buffer.getSpeechTestScore())
                    .fingerTestScore(buffer.getFingerTestScore())
                    .armTestScore(buffer.getArmTestScore())
                    .feedback(buffer.getFeedback())
                    .avgRiskScore(avgRiskScore)
                    .build();

            testRecordRepository.save(newRecord);
            log.info("[저장 완료] TestRecord 저장 완료 - testCount: {}", testCount);

            testRecordBuffer.getAndRemove(memberId);
            log.info("[버퍼 제거] memberId {} 의 버퍼 제거 완료", memberId);

        } else {
            log.info("[검사 미완료] 일부 항목이 입력되지 않음 - memberId: {}", memberId);
        }
            // ✅ 저장 끝난 뒤에만 버퍼 제거
           // testRecordBuffer.getAndRemove(memberId);
        }
    }
