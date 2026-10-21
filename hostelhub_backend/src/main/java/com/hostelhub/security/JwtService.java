package com.hostelhub.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.access-token-minutes:15}")
            long expirationMinutes
    ) {
        byte[] decoded;

        try {
            decoded = java.util.Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "JWT_SECRET must be Base64-encoded.", ex
            );
        }

        if (decoded.length < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET must decode to at least 32 bytes."
            );
        }

        this.key = Keys.hmacShaKeyFor(decoded);
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(String email) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMinutes * 60)))
                .signWith(key)
                .compact();
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(String token, String expectedEmail) {
        Claims claims = parseClaims(token);

        return expectedEmail.equalsIgnoreCase(claims.getSubject())
                && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationSeconds() {
        return expirationMinutes * 60;
    }
}
