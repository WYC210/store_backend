package com.wyc21.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.wyc21.entity.User;
import com.wyc21.service.IUserService;
import com.wyc21.util.JsonResult;
import com.wyc21.util.RedisUtil;
import com.wyc21.util.CookieUtil;
import jakarta.servlet.http.Cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController extends BaseController {
    @Autowired
    private IUserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CookieUtil cookieUtil;

    @PostMapping("/reg")
    public JsonResult<Void> reg(@RequestBody User user) {
        userService.reg(user);
        return new JsonResult<>(OK, null, "注册成功");
    }

    @PostMapping("/login")
    public JsonResult<User> login(@RequestBody User user, HttpServletRequest request, HttpServletResponse response) {
        // 参数验证
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return new JsonResult<>(400, null, "用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return new JsonResult<>(400, null, "密码不能为空");
        }

        User data = userService.login(user.getUsername(), user.getPassword(), request, response);
        return new JsonResult<>(OK, data, "登录成功");
    }

    @PostMapping("/logout")
    public JsonResult<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Integer uid = (Integer) request.getAttribute("uid");
        if (uid != null) {
            // 从Redis中删除token
            redisUtil.deleteToken("token:" + uid);
            // 清除Cookie
            cookieUtil.clearTokenCookie(response);
            return new JsonResult<>(OK, null, "登出成功");
        }
        return new JsonResult<>(OK, null, "用户未登录");
    }

    @GetMapping("/info")
    public JsonResult<User> getUserInfo(HttpServletRequest request) {
        try {
            Integer uid = (Integer) request.getAttribute("uid");
            // String username = (String) request.getAttribute("username");

            if (uid == null) {
                return new JsonResult<>(401, null, "未获取到用户ID");
            }

            User user = userService.getUserById(uid);
            return new JsonResult<>(OK, user);
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "获取用户信息失败：" + e.getMessage());
        }
    }
}
