package com.naebom.stroke.naebom.controller;

import com.naebom.stroke.naebom.dto.LoginResponseDto;
import com.naebom.stroke.naebom.dto.SignupRequestDto;
import com.naebom.stroke.naebom.dto.LoginRequestDto;
import com.naebom.stroke.naebom.service.AuthService;
import com.naebom.stroke.naebom.service.VerificationCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final VerificationCodeService verificationCodeService;

    public AuthController(AuthService authService, VerificationCodeService verificationCodeService) {
        this.authService = authService;
        this.verificationCodeService = verificationCodeService;
    }

    // 1️⃣ 이메일 인증 코드 전송
    @PostMapping("/send-verification")
    public ResponseEntity<Map<String, String>> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        verificationCodeService.generateAndSendCode(email);
        Map<String, String> response = new HashMap<>();
        response.put("message", "인증번호가 이메일로 전송되었습니다.");
        return ResponseEntity.ok(response);
    }

    // 2️⃣ 인증 코드 검증 (이메일 + verificationCode 받기)
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, String>> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String verificationCode = request.get("verificationCode");

        try {
            verificationCodeService.verifyCode(email, verificationCode);
            Map<String, String> response = new HashMap<>();
            response.put("message", "이메일 인증 완료!");
            response.put("email", email); // 인증된 이메일 반환
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 3️⃣ 회원가입 (인증된 이메일만 가능)
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody SignupRequestDto requestDto) {
        try {
            authService.signup(requestDto);
            Map<String, String> response = new HashMap<>();
            response.put("message", "회원가입 성공!");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 4️⃣ 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        LoginResponseDto response = authService.login(requestDto);
        return ResponseEntity.ok(response);
    }

}
