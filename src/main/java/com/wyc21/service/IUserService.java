package com.wyc21.service;

import com.wyc21.entity.User;

// 用户模块业务层接口
public interface IUserService {
    /**
     * 用户注册
     * @param user 用户数据
     */
    void reg(User user);

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户数据
     */
    User login(String username, String password);

    /**
     * 根据用户id获取用户信息
     * @param uid 用户id
     * @return 用户信息
     */
    User getUserById(Integer uid);
}
