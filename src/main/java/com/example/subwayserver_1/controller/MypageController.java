package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.entity.UserDetails;
import com.example.subwayserver_1.repository.UserDetailsRepository;
import com.example.subwayserver_1.util.PasswordUtil;
import com.example.subwayserver_1.util.JwtUtil;
import com.example.subwayserver_1.repository.CalendarRouteRepository; // 추가
import com.example.subwayserver_1.repository.FavoriteRouteRepository; // 추가
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional; // 추가

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mypage")
@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "https://namotigerta.com",
                "https://namotigerta.netlify.app"
        } // 허용할 도메인
)

public class MypageController {

    @Value("${jwt.secret}") // 환경 변수에서 JWT_SECRET 값 로드
    private String secretKey;

    @Autowired
    private JwtUtil jwtUtil; // JwtUtil 주입

    @Autowired
    private PasswordUtil passwordUtil; // PasswordUtil 주입

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private CalendarRouteRepository calendarRouteRepository; // CalendarRouteRepository 주입

    @Autowired
    private FavoriteRouteRepository favoriteRouteRepository; // FavoriteRouteRepository 주입

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
        response.put("profile_picture", user.getProfilePicture()); // 프로필 사진 ID
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
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authorizationHeader,
                                            @RequestBody Map<String, String> passwordRequest) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error_message", "Missing or invalid Authorization header"));
        }

        String token = authorizationHeader.replace("Bearer ", "");
        String username = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        Optional<UserDetails> optionalUser = userDetailsRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error_message", "User not found"));
        }

        UserDetails user = optionalUser.get();
        String currentPassword = passwordRequest.get("current_password");
        String newPassword = passwordRequest.get("new_password");

        // 현재 비밀번호 확인
        if (!PasswordUtil.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(400).body(Map.of("error_message", "Current password is incorrect"));
        }

        // 새 비밀번호 암호화 후 저장
        String encryptedNewPassword = PasswordUtil.encodePassword(newPassword);
        user.setPassword(encryptedNewPassword);
        userDetailsRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * 개인정보 수정 요청
     */
    @PatchMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestHeader("Authorization") String authorizationHeader,
                                               @RequestBody Map<String, Object> updateRequest) {
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

        // 요청 받은 필드에 대해 업데이트
        if (updateRequest.containsKey("name")) {
            user.setName((String) updateRequest.get("name"));
        }

        if (updateRequest.containsKey("nickname")) {
            user.setNickname((String) updateRequest.get("nickname"));
        }

        if (updateRequest.containsKey("email")) {
            user.setEmail((String) updateRequest.get("email"));
        }

        if (updateRequest.containsKey("isClimateCardEligible")) {
            user.setIsClimateCardEligible((Boolean) updateRequest.get("isClimateCardEligible"));
        }

        if (updateRequest.containsKey("profile_picture")) {
            user.setProfilePicture((Integer) updateRequest.get("profile_picture")); // 프로필 사진 업데이트
        }

        // 업데이트된 사용자 정보 저장
        userDetailsRepository.save(user);

        // 응답 반환
        return ResponseEntity.ok(Map.of("data", Map.of(
                "name", user.getName(),
                "nickname", user.getNickname(),
                "email", user.getEmail(),
                "isClimateCardEligible", user.getIsClimateCardEligible(),
                "profile_picture", user.getProfilePicture()
        ), "message", "Profile updated successfully"));
    }

    /**
     * 회원탈퇴
     */
    @Transactional
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> requestBody) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error_message", "Missing or invalid Authorization header"));
        }

        // 입력된 비밀번호 추출
        String inputPassword = requestBody.get("password");
        if (inputPassword == null || inputPassword.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("error_message", "Password is required"));
        }

        try {
            // JWT에서 사용자명 추출
            String username = JwtUtil.extractUsername(authorizationHeader);

            // 사용자 조회
            Optional<UserDetails> optionalUser = userDetailsRepository.findByUsername(username);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error_message", "User not found"));
            }

            UserDetails user = optionalUser.get();

            // 비밀번호 확인
            if (!PasswordUtil.matches(inputPassword, user.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("error_message", "Password does not match"));
            }

            // 사용자 아이디를 통해 연관된 데이터 삭제
            // 캘린더 데이터 삭제
            calendarRouteRepository.deleteByUserId(user.getId());

            // 즐겨찾기 데이터 삭제
            favoriteRouteRepository.deleteByUserId(user.getId());

            // 사용자 삭제
            userDetailsRepository.delete(user);

            return ResponseEntity.ok(Map.of("message", "Account and related data deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("error_message", e.getMessage()));
        }
    }
}
