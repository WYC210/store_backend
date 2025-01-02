package com.wyc21.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000; // 7天
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(Integer uid, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRE_TIME);
        
        return Jwts.builder()
                .setSubject(username)
                .claim("uid", uid)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
} 