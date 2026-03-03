package com.example.PersonalFinanceTracker.security;

import com.example.PersonalFinanceTracker.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * JwtService
 * - Tạo token
 * - Lấy email từ token
 * - Validate token
 */
@Service
public class JwtService {

    // SECRET_KEY phải >= 32 ký tự
    private final String SECRET_KEY = "mysecretkeymysecretkeymysecretkey123";

    /**
     * Tạo key ký JWT từ SECRET_KEY
     */
    private Key getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT Token
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail()) // subject = email
                .claim("userId", user.getId())
                .claim("fullName", user.getFullName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 ngày
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Lấy email từ token
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Lấy toàn bộ claims từ JWT
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey()) // dùng key của bạn
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Kiểm tra token hợp lệ
     */
    public boolean isTokenValid(String token, User user) {
        final String email = extractEmail(token);
        return (email.equals(user.getEmail()) && !isTokenExpired(token));
    }

    /**
     * Kiểm tra token hết hạn
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Lấy thời gian hết hạn của token (ISO-8601)
     */
    public String getExpireTime(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.toInstant().toString(); // format: 2025-05-12T15:30:00Z
    }
}