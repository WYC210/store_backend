package com.wyc21.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wyc21.util.PasswordUtils;
import com.wyc21.entity.User;
import com.wyc21.mapper.UserMapper;
import com.wyc21.service.IUserService;
import com.wyc21.service.ex.InsertException;
import com.wyc21.service.ex.UsernameDuplicatedException;
import com.wyc21.service.ex.UserNotFoundException;
import com.wyc21.service.ex.PasswordNotMatchException;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void reg(User user) {
        // 根据用户名查询用户数据,判断用户是否被注册过
        User result = userMapper.findByUsername(user.getUsername());
        if (result != null) {
            throw new UsernameDuplicatedException("用户名已被注册");
        }
        // 密码加密
        String oldPassword = user.getPassword();
        String newPassword = PasswordUtils.hashPassword(oldPassword);
        user.setPassword(newPassword);

        // 补全数据
        user.setIsDelete(0);
        user.setCreatedUser(user.getUsername());
        user.setModifiedUser(user.getUsername());
        Date date = new Date();
        user.setCreatedTime(date);
        user.setModifiedTime(date);

        // 执行注册
        Integer rows = userMapper.insert(user);
        if (rows != 1) {
            throw new InsertException("注册时发生未知错误");
        }
    }

    @Override
    public User login(String username, String password) {
        // 根据用户名查询用户数据
        User result = userMapper.findByUsername(username);
        if (result == null) {
            throw new UserNotFoundException("用户不存在");
        }

        // 判断用户是否被删除
        if (result.getIsDelete() == 1) {
            throw new UserNotFoundException("用户不存在");
        }

        // 判断密码是否匹配
        String hashedPassword = result.getPassword();
        if (!PasswordUtils.checkPassword(password, hashedPassword)) {
            throw new PasswordNotMatchException("密码错误");
        }

        // 创建新的User对象，仅包含必要的信息返回
        User user = new User();
        user.setUid(result.getUid());
        user.setUsername(result.getUsername());
        user.setAvatar(result.getAvatar());

        return user;
    }
}