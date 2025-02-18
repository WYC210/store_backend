// package com.wyc21.service;

// import com.wyc21.util.JwtUtil;
// import io.jsonwebtoken.Claims;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.security.core.context.SecurityContextHolder;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;
// import com.wyc21.interceptor.TokenInterceptor;

// class TokenInterceptorTest {

// @InjectMocks
// private TokenInterceptor tokenInterceptor;

// @Mock
// private JwtUtil jwtUtil;

// @Mock
// private RedisTemplate<String, String> redisTemplate;

// @Mock
// private HttpServletRequest request;

// @Mock
// private HttpServletResponse response;

// @Mock
// private FilterChain filterChain;

// @BeforeEach
// void setUp() {
// MockitoAnnotations.openMocks(this);
// SecurityContextHolder.clearContext();
// }

// @Test
// void shouldPassForPublicEndpoints() throws Exception {
// // 设置公开路径
// when(request.getRequestURI()).thenReturn("/products");
// when(request.getMethod()).thenReturn("GET");

// // tokenInterceptor.doFilterInternal(request, response, filterChain);

// verify(filterChain).doFilter(request, response);
// verifyNoInteractions(jwtUtil);
// }

// @Test
// void shouldFailForMissingToken() throws Exception {
// // 设置需要认证的路径
// when(request.getRequestURI()).thenReturn("/users/profile");
// when(request.getMethod()).thenReturn("GET");
// when(request.getHeader("Authorization")).thenReturn(null);

// // tokenInterceptor.doFilterInternal(request, response, filterChain);

// verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
// verifyNoInteractions(filterChain);
// }

// @Test
// void shouldPassForValidToken() throws Exception {
// // 设置需要认证的路径
// when(request.getRequestURI()).thenReturn("/users/profile");
// when(request.getMethod()).thenReturn("GET");
// when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");

// Claims mockClaims = mock(Claims.class);
// when(mockClaims.getSubject()).thenReturn("testuser");
// when(mockClaims.get("uid")).thenReturn(1L);
// when(jwtUtil.validateToken("valid-token")).thenReturn(mockClaims);

// // tokenInterceptor.doFilterInternal(request, response, filterChain);

// verify(request).setAttribute("uid", 1L);
// verify(request).setAttribute("username", "testuser");
// verify(filterChain).doFilter(request, response);
// assertNotNull(SecurityContextHolder.getContext().getAuthentication());
// }

// @Test
// void shouldHandleTokenValidationException() throws Exception {
// // 设置需要认证的路径
// when(request.getRequestURI()).thenReturn("/users/profile");
// when(request.getMethod()).thenReturn("GET");
// when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");

// when(jwtUtil.validateToken("invalid-token")).thenThrow(new
// RuntimeException("Token验证失败"));

// // tokenInterceptor.doFilterInternal(request, response, filterChain);

// verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
// verifyNoInteractions(filterChain);
// }

// @Test
// void shouldNotFilterOptionsRequests() throws Exception {
// when(request.getMethod()).thenReturn("OPTIONS");

// // boolean shouldNotFilter = tokenInterceptor.shouldNotFilter(request);

// assertTrue(shouldNotFilter);
// }

// @Test
// void shouldNotFilterWhitelistedPaths() throws Exception {
// when(request.getRequestURI()).thenReturn("/products/1");
// when(request.getMethod()).thenReturn("GET");

// // boolean shouldNotFilter = tokenInterceptor.shouldNotFilter(request);

// // assertTrue(shouldNotFilter);
// }
// }