package com.wyc21.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wyc21.util.JsonResult;
import com.wyc21.util.JwtUtil;
import com.wyc21.util.RedisUtil;
import com.wyc21.util.IpUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private IpUtil ipUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 获取请求路径
        String path = request.getRequestURI();
        // 显示完整路径
        System.out.println("请求路径：" + path);
        // 检查是否是不需要拦截的路径
        if (path.startsWith("/products") ||
                path.startsWith("/categories") ||
                path.equals("/users/login") ||
                path.equals("/users/reg")) {
            return true; // 直接放行
        }

        // 放行OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 从Cookie中获取token
        Cookie[] cookies = request.getCookies();
        String token = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("AUTH-TOKEN".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            handleError(response, "未登录");
            return false;
        }

        try {
            // 如果token以Bearer 开头，去掉这个前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 解析token
            Claims claims = jwtUtil.parseToken(token);
            Long uid = claims.get("uid", Long.class);
            String tokenIp = claims.get("ip", String.class);
            String tokenIpLocation = claims.get("ipLocation", String.class);

            // 获取当前请求的IP信息
            String currentIp = ipUtil.getIpAddress(request);
            String currentIpLocation = ipUtil.getIpLocation(currentIp);

            // 验证IP和地理位置
            if (!tokenIp.equals(currentIp) || !tokenIpLocation.equals(currentIpLocation)) {
                // 删除Redis中的token
                redisUtil.deleteToken("token:" + uid);
                handleError(response, "检测到异常登录，请重新登录");
                return false;
            }

            // 从Redis验证token
            String storedToken = redisUtil.getToken("token:" + uid);
            if (storedToken == null || !storedToken.equals(token)) {
                handleError(response, "token已失效，请重新登录");
                return false;
            }

            // 将用户信息存入请求属性中
            request.setAttribute("uid", uid);
            request.setAttribute("username", claims.getSubject());

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