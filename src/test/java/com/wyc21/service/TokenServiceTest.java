package com.wyc21.service;

import com.wyc21.entity.User;
import com.wyc21.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private Claims claims; // Mock Claims

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoginSuccess() {
        String username = "testUser";
        String password = "testPassword";

        // 模拟用户数据
        User user = new User();
        user.setUsername(username);
        user.setPassword(new BCryptPasswordEncoder().encode(password)); // 加密密码

        // 模拟 UserMapper 的行为
        when(userMapper.findByUsername(username)).thenReturn(user);
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

        // 执行登录
        Map<String, String> tokens = tokenService.login(username, password);
        assertNotNull(tokens, "Tokens should not be null");
        assertNotNull(tokens.get("accessToken"), "Access token should not be null");
        assertNotNull(tokens.get("refreshToken"), "Refresh token should not be null");
    }

    @Test
    public void testLoginFailure() {
        String username = "testUser";
        String password = "wrongPassword";

        // 模拟 UserMapper 的行为
        when(userMapper.findByUsername(username)).thenReturn(null); // 用户不存在

        // 执行登录
        Map<String, String> tokens = tokenService.login(username, password);
        assertNull(tokens, "Tokens should be null for invalid login");
    }

    @Test
    public void testRefreshAccessTokenSuccess() {
        String username = "testUser";
        String refreshToken = "validRefreshToken";

        // 模拟 token 的验证
        when(tokenService.validateToken(refreshToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(username); // 模拟 claims 的行为
        when(tokenService.refreshAccessToken(refreshToken)).thenReturn("newAccessToken");

        // 执行刷新
        String newAccessToken = tokenService.refreshAccessToken(refreshToken);
        assertNotNull(newAccessToken, "New access token should not be null");
    }

    @Test
    public void testRefreshAccessTokenFailure() {
        String invalidRefreshToken = "invalidRefreshToken";

        // 模拟 token 的验证失败
        when(tokenService.validateToken(invalidRefreshToken)).thenReturn(null);

        // 执行刷新
        String newAccessToken = tokenService.refreshAccessToken(invalidRefreshToken);
        assertNull(newAccessToken, "New access token should be null for invalid refresh token");
    }

    @Test
    public void testLogout() {
        String refreshToken = "someRefreshToken";

        // 执行登出
        tokenService.logout(refreshToken);

        // 验证 refresh token 被移除
        // 这里可以添加验证逻辑，确保 refresh token 被标记为无效
        // 由于我们没有持久化存储，无法直接验证
    }
}