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
    private static final long ACCESS_TOKEN_EXPIRE = 5 * 60 * 1000;
    private static final Key JWT_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(Integer uid, String username, String ip, String ipLocation) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE);

        return Jwts.builder()
                .setSubject(username)
                .claim("uid", uid)
                .claim("ip", ip)
                .claim("ipLocation", ipLocation)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(JWT_KEY)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(JWT_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}