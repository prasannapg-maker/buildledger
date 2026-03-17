package com.buildledger.iam.security;

import com.buildledger.iam.config.JwtProperties;
import com.buildledger.iam.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(
                jwtProperties.getSecret().getBytes()
            )
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a signed access token for the given user.
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        claims.put("type", "ACCESS");

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(user.getId()))
                .claims(claims)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration()))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate a signed refresh token for the given user.
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(user.getId()))
                .claims(claims)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiration()))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Parse and validate a token, returning the Claims object.
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract user ID from token.
     */
    public Long extractUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    /**
     * Extract role from token.
     */
    public String extractRole(String token) {
        return (String) parseToken(token).get("role");
    }

    /**
     * Extract email from token.
     */
    public String extractEmail(String token) {
        return (String) parseToken(token).get("email");
    }

    /**
     * Extract unique JWT ID (jti) used for blacklisting.
     */
    public String extractTokenId(String token) {
        return parseToken(token).getId();
    }

    /**
     * Extract expiration date from token.
     */
    public Date extractExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    /**
     * Validate token structure, signature, and expiry.
     * Returns true if valid; exceptions are caught and logged.
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public long getAccessTokenExpirationMs() {
        return jwtProperties.getAccessTokenExpiration();
    }

    public long getRefreshTokenExpirationMs() {
        return jwtProperties.getRefreshTokenExpiration();
    }
}
