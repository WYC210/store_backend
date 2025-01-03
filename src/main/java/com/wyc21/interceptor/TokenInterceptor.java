package com.wyc21.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wyc21.util.JsonResult;
import com.wyc21.util.JwtUtil;
import com.wyc21.util.RedisUtil;
import io.jsonwebtoken.Claims;
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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
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
            Integer uid = claims.get("uid", Integer.class);

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
            e.printStackTrace(); // 添加这行来查看具体错误
            handleError(response, "token验证失败: " + e.getMessage());
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