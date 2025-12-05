package com.pizza.libms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.issuer}")
    private String issuer;
    @Value("${jwt.expire-minutes}")
    private long expireMinutes;

    private SecretKey key() { return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }
    private JwtParser parser() { return Jwts.parser().verifyWith(key()).build(); }

    public String generateToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expireMinutes * 60)))
                .signWith(key())
                .compact();
    }

    public boolean validate(String token) {
        try {
            JwtParser parser = parser();
            Jws<Claims> claimsJws = parser.parseSignedClaims(token);
            Claims body = claimsJws.getPayload();
            if (body.getExpiration() == null || body.getExpiration().before(new Date())) {
                return false;
            }
            if (issuer != null && !issuer.isBlank() && !issuer.equals(body.getIssuer())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsername(String token) {
        JwtParser parser = parser();
        return parser.parseSignedClaims(token).getPayload().getSubject();
    }

    public String getRole(String token) {
        JwtParser parser = parser();
        Object role = parser.parseSignedClaims(token).getPayload().get("role");
        return role == null ? null : role.toString();
    }
}
