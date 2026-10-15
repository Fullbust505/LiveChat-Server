package com.example.livechat.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * Génération et validation de JWT, sur le modèle du TP5 (JJWT 0.12.x).
 */
@Component
public class JwtUtil {

    // Clé secrète HS256 (minimum 256 bits) — 1h de validité
    private final SecretKey secretKey = Keys.hmacShaKeyFor(
            "livechatSecretKeyLivechatSecretKey123!".getBytes());

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extraireUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }
}
