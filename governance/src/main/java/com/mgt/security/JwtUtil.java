package com.mgt.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.access.secret:govt-mgt-access-secret-minimum-32chars-key!}")
    private String accessSecret;

    @Value("${jwt.refresh.secret:govt-mgt-refresh-secret-minimum-32chars-key!}")
    private String refreshSecret;

    @Value("${jwt.access.expiry-ms:7200000}")
    private long accessExpiryMs;

    @Value("${jwt.refresh.expiry-ms:604800000}")
    private long refreshExpiryMs;

    // ── Secret: minimum 32 bytes, zero-pad if short ──────────────────────
    private SecretKey getAccessKey() {
        return Keys.hmacShaKeyFor(padToMinLength(accessSecret, 32));
    }

    private SecretKey getRefreshKey() {
        return Keys.hmacShaKeyFor(padToMinLength(refreshSecret, 32));
    }

    private byte[] padToMinLength(String secret, int minBytes) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length >= minBytes) return bytes;
        return Arrays.copyOf(bytes, minBytes);
    }

    // ── Generate Access Token ─────────────────────────────────────────────
    public String generateAccessToken(int userId, String email, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role",  role)
                .claim("type",  "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiryMs))
                .signWith(getAccessKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ── Generate Refresh Token ───────────────────────────────────────────
    public String generateRefreshToken(int userId, String email) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("type",  "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiryMs))
                .signWith(getRefreshKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ── Extract from Access Token ────────────────────────────────────────
    public Integer getUserId(String token) {
        return Integer.parseInt(getAccessClaims(token).getSubject());
    }

    public String getRole(String token) {
        return getAccessClaims(token).get("role", String.class);
    }

    public String getEmail(String token) {
        return getAccessClaims(token).get("email", String.class);
    }

    // ── Validate ─────────────────────────────────────────────────────────
    public boolean isAccessTokenValid(String token) {
        try {
            Claims claims = getAccessClaims(token);
            if (claims.getExpiration().before(new Date())) return false;
            // type claim থাকলে check; না থাকলে (পুরনো token) pass
            String type = claims.get("type", String.class);
            return type == null || "access".equals(type);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = getRefreshClaims(token);
            if (claims.getExpiration().before(new Date())) return false;
            String type = claims.get("type", String.class);
            return type == null || "refresh".equals(type);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ── Extract from Refresh Token ───────────────────────────────────────
    public Integer getUserIdFromRefresh(String token) {
        return Integer.parseInt(getRefreshClaims(token).getSubject());
    }

    public String getEmailFromRefresh(String token) {
        return getRefreshClaims(token).get("email", String.class);
    }

    public long getAccessExpiryMs() { return accessExpiryMs; }

    // ── Private ──────────────────────────────────────────────────────────
    private Claims getAccessClaims(String token) {
        return Jwts.parser().verifyWith(getAccessKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    private Claims getRefreshClaims(String token) {
        return Jwts.parser().verifyWith(getRefreshKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    @Deprecated
    public boolean isValid(String token) { return isAccessTokenValid(token); }

    @Deprecated
    public String generateToken(int userId, String email, String role) {
        return generateAccessToken(userId, email, role);
    }
}
