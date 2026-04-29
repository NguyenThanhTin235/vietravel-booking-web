package com.vietravel.booking.service.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService{

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-minutes:60}")
    private long accessMinutes;

    public String generateAccessToken(Long userId,String email,String role){
        var key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        var now=Instant.now();
        var exp=now.plusSeconds(accessMinutes*60);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(Map.of("email",email,"role",role))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Map<String,Object> parseClaims(String token){
        var key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        var jws=Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        return jws.getPayload();
    }
}
