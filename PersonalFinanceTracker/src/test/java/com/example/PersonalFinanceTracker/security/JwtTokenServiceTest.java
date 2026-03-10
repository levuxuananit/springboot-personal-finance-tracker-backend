package com.example.PersonalFinanceTracker.security;

import java.util.Optional;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.PersonalFinanceTracker.entity.User;
import com.example.PersonalFinanceTracker.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys; 

class JwtTokenServiceTest {

    private static final String BASE64_SECRET = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=";

    @Test
    void extractUserId_shouldUseUserIdClaim_whenPresent() {
        UserRepository userRepository = mock(UserRepository.class);
        JwtTokenService service = new JwtTokenService(BASE64_SECRET, userRepository);

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(BASE64_SECRET));
        String token = Jwts.builder()
                .claim("userId", 123L)
                .signWith(key)
                .compact();

        assertEquals(123L, service.extractUserId(token));
    }

    @Test
    void extractUserId_shouldUseNumericSubject_whenPresent() {
        UserRepository userRepository = mock(UserRepository.class);
        JwtTokenService service = new JwtTokenService(BASE64_SECRET, userRepository);

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(BASE64_SECRET));
        String token = Jwts.builder()
                .subject("7")
                .signWith(key)
                .compact();

        assertEquals(7L, service.extractUserId(token));
    }

    @Test
    void extractUserId_shouldResolveEmailSubject_viaUserRepository() {
        UserRepository userRepository = mock(UserRepository.class);
        JwtTokenService service = new JwtTokenService(BASE64_SECRET, userRepository);

        User user = new User();
        user.setId(55L);

        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(BASE64_SECRET));
        String token = Jwts.builder()
                .subject("a@b.com")
                .signWith(key)
                .compact();

        assertEquals(55L, service.extractUserId(token));
    }

    @Test
    void extractUserId_shouldThrow_whenNoUserIdClaimAndNoSubject() {
        UserRepository userRepository = mock(UserRepository.class);
        JwtTokenService service = new JwtTokenService(BASE64_SECRET, userRepository);

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(BASE64_SECRET));
        String token = Jwts.builder()
                .claim("foo", "bar")
                .signWith(key)
                .compact();

        assertThrows(IllegalArgumentException.class, () -> service.extractUserId(token));
    }
}

