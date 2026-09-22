package com.example.distributedchatservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    // 256-bit secret key for HMAC-SHA256 (minimum 32 characters)
    private static final String SECRET_STRING = "discord-clone-distributed-systems-jwt-secret-key-2026-production";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    // Token lifespan: 7 days
    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 24 * 7;

    /**
     * Process 1.2: Signs a stateless token embedding user claims.
     */
    public String generateToken(UUID userId, String username) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key)
                .compact();
    }

    /**
     * Process 1.6: Validates token signature and extracts claims.
     */
    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(validateAndGetClaims(token).getSubject());
    }

    public String extractUsername(String token) {
        return validateAndGetClaims(token).get("username", String.class);
    }
}