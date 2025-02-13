package com.wyc21.service;

import com.wyc21.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.wyc21.interceptor.TokenInterceptor;

class TokenInterceptorTest {

    @InjectMocks
    private TokenInterceptor tokenInterceptor;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldPassForPublicEndpoints() throws Exception {
        // 设置公开路径
        when(request.getRequestURI()).thenReturn("/products");

        boolean result = tokenInterceptor.preHandle(request, response, null);

        assertTrue(result);
    }

    @Test
    void shouldFailForMissingToken() throws Exception {
        // 设置需要认证的路径
        when(request.getRequestURI()).thenReturn("/users/profile");
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = tokenInterceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void shouldPassForValidToken() throws Exception {
        // 设置需要认证的路径
        when(request.getRequestURI()).thenReturn("/users/profile");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");

        Claims mockClaims = mock(Claims.class);
        when(mockClaims.get("uid")).thenReturn(1L);
        when(mockClaims.get("username")).thenReturn("testuser");
        when(jwtUtil.validateToken("valid-token")).thenReturn(mockClaims);
        when(jwtUtil.isTokenExpired(mockClaims)).thenReturn(false);

        boolean result = tokenInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(request).setAttribute("uid", 1L);
        verify(request).setAttribute("username", "testuser");
    }

    @Test
    void shouldRefreshExpiredToken() throws Exception {
        // 设置需要认证的路径和过期token
        when(request.getRequestURI()).thenReturn("/users/profile");
        when(request.getHeader("Authorization")).thenReturn("Bearer expired-token");

        Claims mockClaims = mock(Claims.class);
        when(mockClaims.get("uid")).thenReturn(1L);
        when(mockClaims.get("username")).thenReturn("testuser");
        when(jwtUtil.validateToken("expired-token")).thenReturn(mockClaims);
        when(jwtUtil.isTokenExpired(mockClaims)).thenReturn(true);

        // 模拟刷新token
        Cookie[] cookies = new Cookie[] { new Cookie("refresh_token", "valid-refresh-token") };
        when(request.getCookies()).thenReturn(cookies);
        when(valueOperations.get("refresh_token:1")).thenReturn("valid-refresh-token");
        when(jwtUtil.generateAccessToken(1L, "testuser", "127.0.0.1", "中国")).thenReturn("new-token");

        boolean result = tokenInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(response).setHeader("Authorization", "Bearer new-token");
    }
}