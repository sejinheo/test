package com.naebom.stroke.naebom.service;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import com.naebom.stroke.naebom.utils.LevenshteinUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class SpeechToTextService {

    private static final Logger logger = LoggerFactory.getLogger(SpeechToTextService.class);
    private static final int SAMPLE_RATE = 16000; // 표준 음성 샘플 레이트

    /** Base64 오디오 데이터를 받아 STT 변환 후 유사도 점수 계산 */
    public double evaluateSpeech(String base64Audio, String expectedText) {
        File audioFile = null;
        try {
            // Base64 → M4A → FLAC 변환
            audioFile = convertBase64ToFlac(base64Audio);

            if (audioFile.length() == 0) {
                logger.error("변환된 FLAC 파일 크기 - 0바이트");
                return 0;
            }

            // Google Speech API 호출하여 음성 → 텍스트 변환
            String recognizedText = transcribeSpeech(audioFile);

            if (recognizedText.isEmpty()) {
                logger.warn("음성이 감지되지 않음");
                return 0;
            }

            // Levenshtein 거리 계산을 통한 유사도 점수 반환
            int distance = LevenshteinUtil.levenshteinDistance(expectedText, recognizedText);
            int maxLength = Math.max(expectedText.length(), recognizedText.length());
            double rawScore = 100 - ((double) distance / maxLength * 100);
            double score = Math.max(0, Math.round(rawScore * 10.0) / 10.0);

            logger.info("평가 완료: 예상='{}', 인식된='{}', 점수={}", expectedText, recognizedText, score);
            return score;

        } catch (Exception e) {
            logger.error("STT 평가 중 오류 발생: {}", e.getMessage(), e);
            return 0;
        } finally {
            deleteFile(audioFile);
        }
    }

    /** Base64 인코딩된 오디오 데이터를 FLAC 파일로 변환 (M4A → FLAC) */
    private File convertBase64ToFlac(String base64Audio) throws Exception {
        // Base64 디코딩
        byte[] audioBytes = Base64.getDecoder().decode(base64Audio);
        File m4aFile = File.createTempFile("input_audio", ".m4a");
        File flacFile = File.createTempFile("converted_audio", ".flac");

        // M4A 파일로 저장
        Files.write(Paths.get(m4aFile.getAbsolutePath()), audioBytes);
        logger.info("M4A 원본 파일 저장 완료: {}, 크기: {} bytes", m4aFile.getAbsolutePath(), m4aFile.length());

        // FFmpeg 실행 (경로를 직접 지정하는 것이 좋음)
        String ffmpegPath = "/opt/homebrew/bin/ffmpeg";
        ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegPath, "-y", "-i", m4aFile.getAbsolutePath(),
                "-ar", "16000", "-ac", "1", "-sample_fmt", "s16",
                flacFile.getAbsolutePath()
        );
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // FFmpeg 로그 실시간 출력
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("FFmpeg Output: {}", line);
            }
        }

        // 변환이 너무 오래 걸릴 경우 30초 후 강제 종료
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            process.destroy();
            throw new IOException("FFmpeg 변환 시간 초과");
        }

        // 변환된 FLAC 파일 크기 확인
        if (flacFile.length() == 0) {
            throw new IOException("M4A → FLAC 변환 실패 - 0바이트 파일 생성됨");
        }

        logger.info("M4A → FLAC 변환 완료: {}, 크기: {} bytes", flacFile.getAbsolutePath(), flacFile.length());

        // 임시 M4A 파일 삭제
        deleteFile(m4aFile);

        return flacFile;
    }

    /** Google Cloud Speech API를 사용하여 STT 변환 */
    private String transcribeSpeech(File audioFile) throws Exception {
        try (SpeechClient speechClient = SpeechClient.create()) {
            byte[] audioBytes = Files.readAllBytes(audioFile.toPath());
            ByteString audioData = ByteString.copyFrom(audioBytes);

            // STT 요청 전에 데이터 크기 검사
            if (audioBytes.length < 1000) {
                logger.warn("오디오 데이터가 너무 작음: {} bytes", audioBytes.length);
                return "";
            }

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.FLAC) // FLAC 포맷 사용
                    .setSampleRateHertz(SAMPLE_RATE) // 16kHz 설정
                    .setLanguageCode("ko-KR") // 한국어 설정
                    .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioData).build();
            RecognizeResponse response = speechClient.recognize(config, audio);

            // 응답 확인 및 상세 로그 추가
            if (response.getResultsList().isEmpty()) {
                logger.warn("Google STT API 응답이 비어 있음");
                return "";
            }

            String transcript = response.getResultsList().get(0).getAlternatives(0).getTranscript();
            logger.info("변환된 텍스트: {}", transcript);
            return transcript;
        }
    }

    /** 파일 삭제 */
    private void deleteFile(File file) {
        if (file != null && file.exists() && file.delete()) {
            logger.info("삭제된 파일: {}", file.getAbsolutePath());
        }
    }
}
