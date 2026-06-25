package com.sarthak.taskscheduler.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "mysecretkeymysecretkeymysecretkeymysecretkey";

    public String generateToken(String email) {

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + 86400000)
                )
                .signWith(getSignKey())
                .compact();
    }

    public String extractUsername(String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignKey() {

        byte[] keyBytes =
                Decoders.BASE64.decode(
                        java.util.Base64
                                .getEncoder()
                                .encodeToString(
                                        SECRET_KEY.getBytes()
                                )
                );

        return Keys.hmacShaKeyFor(keyBytes);
    }
    public boolean isTokenValid(
            String token,
            String email
    ) {
        return extractUsername(token)
                .equals(email);
    }
}