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
import com.wyc21.util.JwtUtil;
import com.wyc21.util.RedisUtil;
import com.wyc21.util.IpUtil;
import com.wyc21.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private IpUtil ipUtil;

    @Autowired
    private CookieUtil cookieUtil;

    @Override
    public void reg(User user) {
        // 根据用户名查询用户数据,判断用户是否被注册过
        User result = userMapper.findByUsername(user.getUsername());
        if (result != null) {
            throw new UsernameDuplicatedException("用户名已被注册");
        }
        // 密码加密

        user.setPassword(PasswordUtils.hashPassword(user.getPassword()));

        // 补全数据
        user.setIsDelete(0);
        user.setCreatedUser(user.getUsername());
        user.setModifiedUser(user.getUsername());
        Date date = new Date();
        user.setCreatedTime(date);
        user.setModifiedTime(date);
        user.setPower("user");

        // 执行注册
        Integer rows = userMapper.insert(user);
        if (rows != 1) {
            throw new InsertException("注册时发生未知错误");
        }
    }

    @Override
    public User login(String username, String password, HttpServletRequest request, HttpServletResponse response) {
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

        // 获取当前请求的IP信息
        String currentIp = ipUtil.getIpAddress(request);
        String currentIpLocation = ipUtil.getIpLocation(currentIp);

        // 获取存储的IP信息
        String storedIpInfo = redisUtil.getToken("ip:" + result.getUid());
        String[] ipParts = storedIpInfo != null ? storedIpInfo.split("\\|") : new String[]{"unknown", "unknown"};
        String tokenIp = ipParts[0];
        String tokenIpLocation = ipParts[1];

        // 如果存储的IP信息为空，表示用户第一次登录，直接存储当前IP信息
        if (storedIpInfo == null) {
            // 存储IP信息
            redisUtil.setToken("ip:" + result.getUid(), currentIp + "|" + currentIpLocation, 7 * 24 * 60 * 60 * 1000L);
        } else {
            // 验证IP和地理位置
            if (!tokenIp.equals(currentIp) || !tokenIpLocation.equals(currentIpLocation)) {
                // 删除Redis中的token
                redisUtil.deleteToken("token:" + result.getUid());
                throw new RuntimeException("检测到异常登录，请重新登录");
            }
        }

        // 生成token
        String token = jwtUtil.generateToken(result.getUid(), result.getUsername(), currentIp, currentIpLocation);

        // 存储到Redis
        String redisKey = "token:" + result.getUid();
        redisUtil.setToken(redisKey, token, 5 * 60 * 1000L);

        // 设置HttpOnly Cookie
        cookieUtil.setTokenCookie(response, token);

        // 返回用户信息
        User user = new User();
        user.setUid(result.getUid());
        user.setUsername(result.getUsername());
        user.setAvatar(result.getAvatar());
        user.setToken("Bearer " + token);

        return user;
    }

    @Override
    public User getUserById(Integer uid) {
        User result = userMapper.findByUid(uid);
        if (result == null || result.getIsDelete() == 1) {
            throw new UserNotFoundException("用户不存在");
        }

        // 创建新的User对象，仅返回必要的信息
        User user = new User();
        user.setUid(result.getUid());
        user.setUsername(result.getUsername());
        user.setAvatar(result.getAvatar());
        user.setPower(result.getPower());

        return user;
    }
}
