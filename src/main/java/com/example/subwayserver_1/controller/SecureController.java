package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.entity.CalendarRoute;
import com.example.subwayserver_1.repository.CalendarRouteRepository;
import com.example.subwayserver_1.util.JwtUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/secure")
@CrossOrigin(origins = "http://localhost:5173")  // 허용할 도메인 명시
public class SecureController {

    // 인증된 요청 처리
    @GetMapping("/data")
    public ResponseEntity<?> getSecureData(@RequestHeader("Authorization") String token) {
        try {
            // 토큰 검증
            String username = JwtUtil.extractUsername(token.replace("Bearer ", ""));
            return ResponseEntity.ok(Map.of(
                    "message", "Secure data accessed",
                    "username", username
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }
}
