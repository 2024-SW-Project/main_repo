package com.example.subwayserver_1.util;

import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component  // Spring 빈으로 등록
public class PasswordUtil {

    // BCryptPasswordEncoder 인스턴스 생성
    public static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 비밀번호 암호화
    public static String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // 비밀번호 일치 여부 확인
    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
