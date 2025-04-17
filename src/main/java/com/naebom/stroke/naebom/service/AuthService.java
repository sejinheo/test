package com.naebom.stroke.naebom.service;

import com.naebom.stroke.naebom.dto.LoginResponseDto;
import com.naebom.stroke.naebom.dto.SignupRequestDto;
import com.naebom.stroke.naebom.dto.LoginRequestDto;
import com.naebom.stroke.naebom.entity.Member;
import com.naebom.stroke.naebom.repository.MemberRepository;
import com.naebom.stroke.naebom.security.JwtTokenProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate; // Redis 사용

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, StringRedisTemplate redisTemplate) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    // ✅ 회원가입 (이메일 인증 후 가능)
    public void signup(SignupRequestDto requestDto) {
        // 🔹 인증된 이메일인지 확인
        String verified = redisTemplate.opsForValue().get("verified:" + requestDto.getEmail());
        if (verified == null || !verified.equals("true")) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
        }

        // 이메일 중복 확인
        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 확인
        if (!requestDto.getPassword().equals(requestDto.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 🔹 birthDate 변환 (문자열 → LocalDate)
        LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(requestDto.getBirthDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            throw new IllegalArgumentException("생년월일 형식이 잘못되었습니다. (올바른 형식: YYYYMMDD)");
        }

        // Member 엔티티 생성 및 저장
        Member member = new Member();
        member.setName(requestDto.getName());
        member.setEmail(requestDto.getEmail());
        member.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        member.setBirthDate(birthDate);
        member.setGender(requestDto.getGender());

        memberRepository.save(member);

        // 🔹 회원가입 후 인증 정보 삭제 (이메일 인증 재사용 방지)
        redisTemplate.delete("verified:" + requestDto.getEmail());
    }

    // 로그인 시 토큰과 memberId 함께 반환
    public LoginResponseDto login(LoginRequestDto requestDto) {
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다.");
        }

        // JWT 토큰 생성
        String token = jwtTokenProvider.generateToken(member.getEmail());

        // 토큰과 회원 ID 함께 반환
        return new LoginResponseDto(token, member.getId());
    }
}
