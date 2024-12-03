package com.example.subwayserver_1.util;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;  // 추가

import java.util.Date;

@Component  // JwtUtil을 Spring 빈으로 등록
public class JwtUtil {

    // SECRET_KEY를 환경 변수에서 가져옴
    private static final String SECRET_KEY;

    static {
        Dotenv dotenv = Dotenv.configure().load();
        SECRET_KEY = dotenv.get("JWT_SECRET"); // .env에서 JWT_SECRET 값 로드
    }

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1시간 (ms 단위)

    // JWT 생성
    public static String generateToken(String username, Long userId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId) // userId를 클레임에 포함
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // JWT 검증 및 클레임 반환
    public static Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token.replace("Bearer ", "")) // "Bearer " 제거
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token has expired. Please re-login.", e);
        } catch (SignatureException e) {
            throw new RuntimeException("Invalid token signature.", e);
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Malformed token.", e);
        } catch (Exception e) {
            throw new RuntimeException("Invalid token. Please check your credentials.", e);
        }
    }

    // 토큰에서 사용자 ID 추출
    public static Long extractUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        Object userId = claims.get("userId");
        if (userId == null) {
            throw new RuntimeException("User ID not found in token.");
        }
        return Long.valueOf(userId.toString());
    }

    // 토큰에서 사용자 이름 추출
    public static String extractUsername(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }
}
