package com.wyc21.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.wyc21.entity.User;
import com.wyc21.service.IUserService;
import com.wyc21.util.JsonResult;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController extends BaseController {
    @Autowired
    private IUserService userService;

    @PostMapping("/reg")
    public JsonResult<Void> reg(@RequestBody User user) {
        userService.reg(user);
        return new JsonResult<>(OK, null, "注册成功");
    }

    @PostMapping("/login")
    public JsonResult<User> login(@RequestBody User user) {
        User data = userService.login(user.getUsername(), user.getPassword());
        return new JsonResult<>(OK, data);
    }

    @GetMapping("/info")
    public JsonResult<User> getUserInfo(HttpServletRequest request) {
        try {
            Integer uid = (Integer) request.getAttribute("uid");
            String username = (String) request.getAttribute("username");
            
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
