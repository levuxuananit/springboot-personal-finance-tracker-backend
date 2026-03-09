package com.example.PersonalFinanceTracker.security;

import com.example.PersonalFinanceTracker.exception.ResourceNotFoundException;
import com.example.PersonalFinanceTracker.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtTokenService {

    private final SecretKey key;
    private final UserRepository userRepository;

    public JwtTokenService(
            @Value("${app.jwt.secret}") String secret,
            UserRepository userRepository
    ) {
        this.key = Keys.hmacShaKeyFor(decodeSecret(secret));
        this.userRepository = userRepository;
    }

    public Long extractUserId(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        Object userIdClaim = claims.get("userId");
        if (userIdClaim != null) {
            return parseLong(userIdClaim.toString());
        }

        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("JWT subject is missing");
        }

        if (subject.matches("\\d+")) {
            return parseLong(subject);
        }

        // Fallback: subject is email
        return userRepository.findByEmail(subject)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + subject));
    }

    private static Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid user id in JWT: " + value, ex);
        }
    }

    private static byte[] decodeSecret(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ex) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }
}
<<<<<<< HEAD
=======

>>>>>>> feature/budget-list
