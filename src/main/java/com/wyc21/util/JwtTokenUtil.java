package com.wyc21.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.accessTokenExpiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refreshTokenExpiration}")
    private Long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 验证token并返回Claims
     * 
     * @param token 要验证的token
     * @return 如果token有效返回Claims，否则返回null
     */
    public Claims validateToken(String token) {
        try {
            log.info("验证token: {}", token);
            log.info("secret: {}", secret);

            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从token中获取用户ID
     * 
     * @param token JWT token
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = validateToken(token);
            if (claims != null) {
                return claims.get("uid", String.class);
            }
            return null;
        } catch (Exception e) {
            log.error("从token中获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 生成访问令牌
     * 
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT token
     */
    public String generateAccessToken(String userId, String username) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessTokenExpiration, ChronoUnit.SECONDS);

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(username)
                .claim("uid", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 生成刷新令牌
     * 
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT token
     */
    public String generateRefreshToken(String userId, String username) {
        Instant now = Instant.now();
        Instant expiry = now.plus(refreshTokenExpiration, ChronoUnit.SECONDS);

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(username)
                .claim("uid", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 检查token是否过期
     * 
     * @param claims JWT的Claims
     * @return 如果过期返回true，否则返回false
     */
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().toInstant().isBefore(Instant.now());
    }
}