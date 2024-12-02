package com.example.subwayserver_1.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // CSRF 비활성화
                .authorizeHttpRequests()
                .requestMatchers(
                        "/mypage/**",  // 마이페이지 경로는 인증 필요
                        "/subway/save/**" // 저장 관련 경로는 인증 필요
                ).authenticated()
                .anyRequest().permitAll() // 그 외 모든 경로는 항상 허용
                .and()
                .formLogin().disable(); // (선택) 기본 로그인 폼 비활성화

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 암호화를 위한 BCrypt 인코더
    }
}
