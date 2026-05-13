package com.cotales.cotales_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class JwtService {

    private final JwtConfig jwtConfig;

    public String generateToken(Long userId, String username, String email) {
        return buildToken(userId, username, email, jwtConfig.getAccessTokenExpiration());
    }

    public String generateRefreshToken(Long userId, String username, String email) {
        return buildToken(userId, username, email, jwtConfig.getRefreshTokenExpiration());
    }

    public Long extractUserId(String token) {
        return Long.parseLong(claims(token).getSubject());
    }

    public String extractEmail(String token) {
        return claims(token).get("email", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            claims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public long getAccessTokenExpiration() {
        return jwtConfig.getAccessTokenExpiration();
    }

    public long getRefreshTokenExpiration() {
        return jwtConfig.getRefreshTokenExpiration();
    }

    public boolean isProduction() {
        return Boolean.TRUE.equals(jwtConfig.getIsProduction());
    }

    private String buildToken(Long userId, String username, String email, long expiration) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(jwtConfig.getSecretKey())
                .compact();
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
