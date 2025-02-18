package com.wyc21.service;

import com.wyc21.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 用户模块业务层接口
public interface IUserService {
    /**
     * 用户注册
     * 
     * @param user 用户数据
     */
    void reg(User user);

    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @param request  HTTP请求对象，用于获取IP信息
     * @param response HTTP响应对象，用于设置Cookie
     * @return 登录成功的用户数据
     */
    User login(String username, String password, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据用户id获取用户信息
     * 
     * @param uid 用户id
     * @return 用户信息
     */
    User getUserById(String uid);

    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    User updateUserInfo(User user);

    /**
     * 更新用户密码
     * 
     * @param uid 用户id
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(String uid, String oldPassword, String newPassword);
}
