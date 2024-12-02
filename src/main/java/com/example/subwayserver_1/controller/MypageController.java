package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.entity.UserDetails;
import com.example.subwayserver_1.repository.UserDetailsRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mypage")
@CrossOrigin(origins = "http://localhost:5173")  // 허용할 도메인 명시
public class MypageController {

    @Value("${jwt.secret}") // 환경 변수에서 JWT_SECRET 값 로드
    private String secretKey;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    /**
     * 사용자 프로필 조회
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error_message", "Missing or invalid Authorization header"));
        }

        String token = authorizationHeader.replace("Bearer ", "");
        String username = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();

        Optional<UserDetails> optionalUser = userDetailsRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error_message", "User not found"));
        }

        UserDetails user = optionalUser.get();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("profile_picture", "http://example.com/profile/" + user.getId());
        response.put("name", user.getName());
        response.put("username", user.getUsername());
        response.put("nickname", user.getNickname());
        response.put("isClimateCardEligible", user.getIsClimateCardEligible());
        response.put("email", user.getEmail());

        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 변경 요청
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authorizationHeader, @RequestBody Map<String, String> passwordRequest) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error_message", "Missing or invalid Authorization header"));
        }

        String token = authorizationHeader.replace("Bearer ", "");
        String username = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();

        Optional<UserDetails> optionalUser = userDetailsRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error_message", "User not found"));
        }

        UserDetails user = optionalUser.get();
        if (!user.getPassword().equals(passwordRequest.get("current_password"))) {
            return ResponseEntity.status(400).body(Map.of("error_message", "Current password is incorrect"));
        }

        user.setPassword(passwordRequest.get("new_password"));
        userDetailsRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * 개인정보 수정 요청
     */
    @PatchMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestHeader("Authorization") String authorizationHeader, @RequestBody Map<String, Object> updateRequest) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error_message", "Missing or invalid Authorization header"));
        }

        String token = authorizationHeader.replace("Bearer ", "");
        String username = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();

        Optional<UserDetails> optionalUser = userDetailsRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error_message", "User not found"));
        }

        UserDetails user = optionalUser.get();
        if (updateRequest.containsKey("isClimateCardEligible")) {
            user.setIsClimateCardEligible((Boolean) updateRequest.get("isClimateCardEligible"));
        }

        userDetailsRepository.save(user);
        return ResponseEntity.ok(Map.of("data", Map.of("isClimateCardEligible", user.getIsClimateCardEligible()), "message", "Profile updated successfully"));
    }
}