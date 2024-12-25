package com.wyc21.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.wyc21.entity.User;
import com.wyc21.service.IUserService;
import com.wyc21.util.JsonResult;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController extends BaseController {
    @Autowired
    private IUserService userService;

    @PostMapping("/reg")
    public JsonResult<Void> reg(@RequestBody User user) {
        userService.reg(user);
        return new JsonResult<>(OK);
    }

    @PostMapping("/login")
    public JsonResult<User> login(@RequestBody User user) {
        User data = userService.login(user.getUsername(), user.getPassword());
        return new JsonResult<>(OK, data);
    }
}
