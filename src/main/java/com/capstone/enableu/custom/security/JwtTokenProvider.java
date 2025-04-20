package com.capstone.enableu.custom.security;

import com.capstone.enableu.common.security.CustomUserDetails;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final String RESET_CLAIM = "needToReset";

    public Object getCheckResetClaim(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .get(RESET_CLAIM);
    }
    private final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    // Use a predefined base64-encoded key for consistent signing and validation
    @Value("${application.jwt.secret}")
    private String secretKey;
    private final long ACCESS_TOKEN_VALIDITY = 30 * 60 * 1000;  // 30 minutes
    private final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 days

    public String generateAccessToken(String userId, String username, String role, boolean needResetPassword) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put(RESET_CLAIM, needResetPassword);

        return Jwts.builder()
                .setSubject(username)
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String generateRefreshToken(String userId, String username, String role, boolean needResetPassword) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put(RESET_CLAIM, needResetPassword);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Map<String, String> getTokenClaims(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = claimsJws.getBody();
            String userId = claims.get("userId", String.class);
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);

            Map<String, String> tokenInfo = new HashMap<>();
            tokenInfo.put("userId", userId);
            tokenInfo.put("username", username);
            tokenInfo.put("role", role);
            return tokenInfo;

        } catch (Exception e) {
            log.error("Invalid token: " + e.getMessage(), e);
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.error("Invalid token: " + e.getMessage(), e);
            return false;
        }
    }
}
