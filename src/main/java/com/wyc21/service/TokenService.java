package com.wyc21.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.wyc21.mapper.UserMapper;
import com.wyc21.entity.User;
import lombok.extern.slf4j.Slf4j;
import com.wyc21.util.JwtUtil;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration; // 例如：15分钟

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // 例如：7天

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    // 登录方法
    public Map<String, String> login(String username, String password) {
        // 验证用户名密码
        User user = userMapper.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 生成 tokens
            String accessToken = generateAccessToken(user.getUsername(), user.getUid());
            String refreshToken = generateRefreshToken(user.getUsername(), user.getUid());

            log.info("生成的 access token: {}", accessToken);
            log.info("生成的 refresh token: {}", refreshToken);

            // 将 refresh token 存储到 Redis
            storeRefreshToken(username, refreshToken);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);
            return tokens;
        }
        return null;
    }

    // 生成 access token
    private String generateAccessToken(String username, String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // 生成 refresh token
    private String generateRefreshToken(String username, String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // 验证 token
    public Claims validateToken(String token) {
        try {
            log.info("开始验证 token: {}", token);
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            log.info("Token 验证成功，claims: {}", claims);
            return claims;
        } catch (Exception e) {
            log.error("Token 验证失败: {}", e.getMessage());
            return null;
        }
    }

    // 存储 refresh token 到 Redis
    private void storeRefreshToken(String username, String refreshToken) {
        String key = "refresh_token:" + username;
        // 存储 refresh token，并设置过期时间
        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS);
    }

    // 验证存储的 refresh token
    private boolean validateStoredRefreshToken(String username, String refreshToken) {
        String key = "refresh_token:" + username;
        String storedToken = redisTemplate.opsForValue().get(key);
        return refreshToken.equals(storedToken);
    }

    // 刷新 access token
    public String refreshAccessToken(String refreshToken) {
        try {
            Claims claims = validateToken(refreshToken);
            if (claims == null) {
                return null;
            }

            String username = claims.getSubject();
            String userId = claims.get("uid", String.class);

            // 验证 refresh token 是否存在于 Redis 中
            if (!validateStoredRefreshToken(username, refreshToken)) {
                return null;
            }

            // 生成新的 access token
            return generateAccessToken(username, userId);
        } catch (Exception e) {
            log.error("刷新token失败: ", e);
            return null;
        }
    }

    // 获取签名密钥
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 登出
    public void logout(String refreshToken) {
        try {
            Claims claims = validateToken(refreshToken);
            if (claims != null) {
                String username = claims.getSubject();
                String key = "refresh_token:" + username;
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            // 处理异常
        }
    }
}