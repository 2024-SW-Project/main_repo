package com.example.subwayserver_1.controller;

import com.example.subwayserver_1.entity.UserDetails;
import com.example.subwayserver_1.repository.UserDetailsRepository;
import com.example.subwayserver_1.util.PasswordUtil;
import com.example.subwayserver_1.util.JwtUtil;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.transaction.annotation.Transactional;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.util.Random;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/auth")
@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "https://namotigerta.com",
                "https://namotigerta.netlify.app"
        }, // 허용할 도메인
        allowedHeaders = {"Authorization", "Content-Type"}, // 허용할 헤더
        exposedHeaders = {"Authorization", "Refresh-Token"}, // 프론트엔드에서 접근 가능하게 노출
        allowCredentials = "true" // 쿠키 사용 시 true
)

public class AuthController {

    @Autowired
    private UserDetailsRepository userDetailsRepository;
    @Autowired
    private JavaMailSender emailSender;
    private static final String SECRET_KEY;

    static {
        Dotenv dotenv = Dotenv.configure().load();
        SECRET_KEY = dotenv.get("JWT_SECRET"); // .env에서 JWT_SECRET 값 로드

    }

    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60; // 1시간
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7일

//        private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 3; // 3분
//    private static final long REFRESH_TOKEN_EXPIRATION =  1000 * 60 * 10; // 10분




    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserDetails userDetails) {
        // 아이디 중복 체크
        if (isUsernameTaken(userDetails.getUsername())) {
            Long existingUserId = getExistingUserId(userDetails.getUsername());
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("error_message", "Registration failed");
            response.put("userId", existingUserId);
            return ResponseEntity.status(409).body(response);
        }

        // 이메일 중복 체크
        if (isEmailTaken(userDetails.getEmail())) {
            Long existingUserId = getExistingUserIdByEmail(userDetails.getEmail());
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("error_message", "Registration failed");
            response.put("userId", existingUserId);
            return ResponseEntity.status(409).body(response);
        }

        // 닉네임 중복 체크
        if (isNicknameTaken(userDetails.getNickname())) {
            Long existingUserId = getExistingUserIdByNickname(userDetails.getNickname());
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("error_message", "Registration failed");
            response.put("userId", existingUserId);
            return ResponseEntity.status(409).body(response);
        }

        // **비밀번호 암호화**
        String rawPassword = userDetails.getPassword(); // 사용자가 입력한 비밀번호
        String encryptedPassword = PasswordUtil.encodePassword(rawPassword); // 암호화
        userDetails.setPassword(encryptedPassword); // 암호화된 비밀번호를 설정

        // 사용자 저장
        userDetailsRepository.save(userDetails);

        // 회원가입 성공
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Registration successful");
        response.put("userId", userDetails.getId());
        return ResponseEntity.status(201).body(response);
    }


    // 중복된 아이디로 기존 userId 반환
    private Long getExistingUserId(String username) {
        return userDetailsRepository.findByUsername(username)
                .map(UserDetails::getId)
                .orElse(null);
    }

    // 중복된 이메일로 기존 userId 반환
    private Long getExistingUserIdByEmail(String email) {
        return userDetailsRepository.findByEmail(email)
                .map(UserDetails::getId)
                .orElse(null);
    }

    // 중복된 닉네임으로 기존 userId 반환
    private Long getExistingUserIdByNickname(String nickname) {
        return userDetailsRepository.findByNickname(nickname)
                .map(UserDetails::getId)
                .orElse(null);
    }

    /**
     * 아이디 중복 체크
     */
    @GetMapping("/signup/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam("username") String username) {
        boolean isAvailable = !isUsernameTaken(username);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isAvailable", isAvailable);

        if (!isAvailable) {
            response.put("error_message", "Username already exists");
            return ResponseEntity.status(409).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginWithToken(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String rawPassword = loginRequest.get("password");

        Optional<UserDetails> optionalUser = userDetailsRepository.findByUsername(username);

        if (optionalUser.isEmpty() || !PasswordUtil.matches(rawPassword, optionalUser.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error_message", "Invalid username or password"));
        }

        UserDetails user = optionalUser.get();

        // Access Token 생성
        String accessToken = JwtUtil.generateToken(user.getUsername(), user.getId(), ACCESS_TOKEN_EXPIRATION);

        // Refresh Token 생성
        String refreshToken = JwtUtil.generateToken(user.getUsername(), user.getId(), REFRESH_TOKEN_EXPIRATION);
        user.setRefreshToken(refreshToken);
        userDetailsRepository.save(user); // DB 업데이트

        // 응답에 필요한 사용자 정보 구성
        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("user_id", user.getId());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("profile_picture", "http://example.com/profile/" + user.getId());

        // 헤더에 토큰 추가
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + accessToken)
                .header("Refresh-Token", refreshToken)
                .body(userInfo);
    }



    /**
     * 리프레시 토큰으로 엑세스 토큰 재발급
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestHeader("Refresh-Token") String refreshToken) {
        String username;

        // Refresh Token 검증
        try {
            Claims claims = JwtUtil.validateToken(refreshToken);
            username = claims.getSubject();
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(401).body(Map.of("error_message", "Refresh Token has expired. Please login again."));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error_message", "Invalid Refresh Token"));
        }

        // 사용자 확인 및 Refresh Token 일치 여부 검사
        Optional<UserDetails> optionalUser = userDetailsRepository.findByUsername(username);
        if (optionalUser.isEmpty() || !refreshToken.equals(optionalUser.get().getRefreshToken())) {
            return ResponseEntity.status(401).body(Map.of("error_message", "Invalid Refresh Token"));
        }

        // 새로운 Access Token 생성
        String newAccessToken = JwtUtil.generateToken(username, optionalUser.get().getId(), ACCESS_TOKEN_EXPIRATION);

        return ResponseEntity.ok(Map.of("access_token", newAccessToken));
    }


    /**
     * 사용자 프로필 조회
     */
    @GetMapping("/mypage/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error_message", "Missing or invalid Authorization header"));
        }

        String token = authorizationHeader.replace("Bearer ", "");
        String username;

        try {
            Claims claims = JwtUtil.validateToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error_message", "Invalid or expired token"));
        }

        Optional<UserDetails> optionalUser = userDetailsRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error_message", "User not found"));
        }

        UserDetails user = optionalUser.get();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("username", user.getUsername());
        response.put("nickname", user.getNickname());

        return ResponseEntity.ok(response);
    }

    /**
     * 닉네임 중복 체크
     */
    @GetMapping("/signup/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestParam("nickname") String nickname) {
        boolean isAvailable = !isNicknameTaken(nickname);

        // LinkedHashMap을 사용하여 순서 보장
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isAvailable", isAvailable); // 먼저 isAvailable 값 넣기

        if (!isAvailable) {
            response.put("error_message", "Nickname already exists"); // 중복이 있을 때만 error_message 추가
            return ResponseEntity.status(409).body(response); // 중복 발생 시 409 반환
        }

        return ResponseEntity.ok(response); // 사용 가능하면 200 반환
    }

    /**
     * 이메일 중복 체크
     */
    @GetMapping("/signup/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam("email") String email) {
        boolean isAvailable = !isEmailTaken(email);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isAvailable", isAvailable);

        if (!isAvailable) {
            response.put("error_message", "Email already exists");
            return ResponseEntity.status(409).body(response); // 중복이 있을 때 409 반환
        }

        return ResponseEntity.ok(response); // 사용 가능하면 200 반환
    }

    /**
     * 아이디 찾기
     */
    @PostMapping("/find-id")
    public ResponseEntity<?> findUsername(@RequestBody Map<String, String> request) {
        String name = request.get("name"); // 기존 'personal_name'을 'name'으로 수정
        String email = request.get("email");

        // UserDetailsRepository에서 name과 email로 사용자 검색
        Optional<UserDetails> user = userDetailsRepository.findByNameAndEmail(name, email);

        if (user.isPresent()) {
            // 사용자가 존재하면 'username' 반환
            Map<String, String> response = new LinkedHashMap<>();
            response.put("username", user.get().getUsername()); // 'personal_id'를 'username'으로 변경
            return ResponseEntity.ok(response);
        } else {
            // 사용자 정보가 일치하지 않으면 에러 메시지 반환
            Map<String, String> response = new LinkedHashMap<>();
            response.put("error_message", "User not found with provided details");
            return ResponseEntity.status(404).body(response);
        }
    }


    /**
     * 비밀번호 재발급
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");

        // username과 email로 사용자 검색
        Optional<UserDetails> optionalUser = userDetailsRepository.findByUsername(username)
                .filter(user -> user.getEmail().equals(email));

        if (optionalUser.isEmpty()) {
            // 사용자 정보 불일치
            Map<String, String> response = new LinkedHashMap<>();
            response.put("error_message", "User not found with provided details");
            return ResponseEntity.status(404).body(response);
        }

        UserDetails user = optionalUser.get();

        // 임시 비밀번호 생성 및 암호화
        String temporaryPassword = generateTemporaryPassword();
        String encryptedPassword = PasswordUtil.encodePassword(temporaryPassword);

        // 암호화된 비밀번호를 저장
        user.setPassword(encryptedPassword);
        userDetailsRepository.save(user);

        // 이메일로 임시 비밀번호 전송
        sendTemporaryPasswordEmail(email, temporaryPassword);

        // 성공 메시지 반환
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Temporary password has been sent to your email");
        return ResponseEntity.ok(response);
    }


    /**
     * 임시 비밀번호 생성
     */
    private String generateTemporaryPassword() {
        int length = 8;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        Random random = new Random();
        StringBuilder tempPassword = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            tempPassword.append(characters.charAt(index));
        }

        return tempPassword.toString();
    }

    /**
     * 이메일로 임시 비밀번호 전송
     */
    private void sendTemporaryPasswordEmail(String email, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your Temporary Password");
        message.setText("Your temporary password is: " + temporaryPassword + "\n\nPlease change your password after logging in.");
        emailSender.send(message);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error_message", "Missing or invalid Authorization header"));
        }

        try {
            // JWT에서 username 추출
            String username = JwtUtil.extractUsername(authorizationHeader);

            // 사용자 조회 및 Refresh Token 제거
            Optional<UserDetails> optionalUser = userDetailsRepository.findByUsername(username);
            if (optionalUser.isPresent()) {
                UserDetails user = optionalUser.get();
                user.setRefreshToken(null); // Refresh Token 무효화
                userDetailsRepository.save(user); // DB 업데이트
                return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
            } else {
                return ResponseEntity.status(404).body(Map.of("error_message", "User not found"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error_message", e.getMessage()));
        }
    }



    private boolean isUsernameTaken(String username) {
        return userDetailsRepository.findByUsername(username).isPresent();
    }

    private boolean isEmailTaken(String email) {
        return userDetailsRepository.findByEmail(email).isPresent();
    }

    private boolean isNicknameTaken(String nickname) {
        return userDetailsRepository.findByNickname(nickname).isPresent();
    }

    private String generateToken(String username, long expirationTime) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}