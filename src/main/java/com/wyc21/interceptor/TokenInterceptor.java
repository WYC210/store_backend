package com.wyc21.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wyc21.util.JsonResult;
import com.wyc21.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.concurrent.TimeUnit;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 获取请求路径
        String path = request.getRequestURI();

        // 检查是否是不需要拦截的路径
        if (path.startsWith("/products") ||
                path.startsWith("/categories") ||
                path.equals("/users/login") ||
                path.equals("/users/reg")) {
            return true;
        }

        // 从Authorization header获取token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            handleError(response, "未登录");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.validateToken(token);
            if (jwtUtil.isTokenExpired(claims)) {
                // Token过期，尝试使用刷新token
                Cookie[] cookies = request.getCookies();
                String refreshToken = null;
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("refresh_token".equals(cookie.getName())) {
                            refreshToken = cookie.getValue();
                            break;
                        }
                    }
                }

                if (refreshToken == null) {
                    handleError(response, "刷新token不存在");
                    return false;
                }

                Long uid = claims.get("uid", Long.class);
                String storedRefreshToken = redisTemplate.opsForValue().get("refresh_token:" + uid);

                if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
                    handleError(response, "无效的刷新token");
                    return false;
                }

                // 生成新的access token
                String username = claims.get("username", String.class);
                String ip = request.getRemoteAddr(); // 获取用户的IP地址
                String ipLocation = ""; // 这里可以调用方法获取IP位置
                String newToken = jwtUtil.generateAccessToken(uid, username, ip, ipLocation);
                response.setHeader("Authorization", "Bearer " + newToken);

                // 设置新的cookie
                Cookie newCookie = new Cookie("jwt", newToken);
                newCookie.setHttpOnly(true);
                newCookie.setSecure(true);
                newCookie.setPath("/");
                newCookie.setMaxAge(900); // 15分钟
                response.addCookie(newCookie);
            }

            request.setAttribute("uid", claims.get("uid"));
            request.setAttribute("username", claims.get("username"));
            return true;
        } catch (Exception e) {
            handleError(response, "token验证失败");
            return false;
        }
    }

    private void handleError(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(new JsonResult<>(401, null, message)));
    }
}