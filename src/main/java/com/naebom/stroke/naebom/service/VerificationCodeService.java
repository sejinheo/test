package com.naebom.stroke.naebom.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class VerificationCodeService {

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final Map<String, String> codeToEmailMap = new HashMap<>();

    public VerificationCodeService(StringRedisTemplate redisTemplate, JavaMailSender mailSender) {
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
    }

    // ✅ 인증 코드 생성 & 이메일 발송
    public void generateAndSendCode(String email) {
        String code = generateCode();
        redisTemplate.opsForValue().set(email, code, 5, TimeUnit.MINUTES); // 5분 저장
        codeToEmailMap.put(code, email);
        sendEmail(email, code);
    }

    // ✅ 인증 코드 검증 후 Redis에 저장 (회원가입 시 검증)
    public void verifyCode(String email, String verificationCode) {
        String storedCode = redisTemplate.opsForValue().get(email);
        if (storedCode == null || !storedCode.equals(verificationCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        // 🔹 이메일 인증 완료 → Redis에 저장 (30분 동안 유효)
        redisTemplate.opsForValue().set("verified:" + email, "true", 30, TimeUnit.MINUTES);
        redisTemplate.delete(email); // 인증 코드 삭제
    }

    // 랜덤 코드 생성
    private String generateCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    // 이메일 발송
    private void sendEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Naebom 인증 코드");
        message.setText("인증 코드: " + code + "\n인증 코드는 5분간 유효합니다.");
        mailSender.send(message);
    }
}
