package com.wyc21.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.wyc21.service.TokenService;
import com.wyc21.util.JsonResult;
import com.wyc21.entity.User; // 假设有一个 User 实体类
import com.wyc21.service.IUserService; // 假设有一个 IUserService 处理用户逻辑
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private TokenService tokenService; // 处理 token 逻辑

    @Autowired
    private IUserService userService; // 处理用户逻辑

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public JsonResult<Map<String, String>> login(@RequestBody User user) {
        log.info("Attempting login for user: {}", user.getUsername());

        Map<String, String> tokens = tokenService.login(user.getUsername(), user.getPassword());
        if (tokens == null) {
            log.warn("Login failed for user: {}", user.getUsername());
            return new JsonResult<>(401, null, "用户名或密码错误");
        }

        log.info("Login successful for user: {}", user.getUsername());
        return new JsonResult<>(200, tokens, "登录成功");
    }

    /**
     * 刷新 token
     */
    @PostMapping("/refresh")
    public JsonResult<String> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        log.info("Received我是续签token,续签的是哈哈哈-- refresh token: {}", refreshToken);
        if (refreshToken == null) {
            return new JsonResult<>(400, null, "Refresh token is required");
        }

        String newAccessToken = tokenService.refreshAccessToken(refreshToken);
        if (newAccessToken == null) {
            log.warn("Invalid refresh token: {}", refreshToken);
            return new JsonResult<>(401, null, "Invalid refresh token");
        }

        return new JsonResult<>(200, newAccessToken, "Token refreshed successfully");
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public JsonResult<Void> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null) {
            return new JsonResult<>(400, null, "Refresh token is required");
        }

        tokenService.logout(refreshToken);
        return new JsonResult<>(200, null, "登出成功");
    }
}