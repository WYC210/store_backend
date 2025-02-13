package com.wyc21.service.impl;

import java.util.Date;
import java.time.LocalDateTime;

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
import com.wyc21.util.SnowflakeIdGenerator;
import com.wyc21.mapper.IdGeneratorMapper;
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

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    private IdGeneratorMapper idGeneratorMapper;

    @Override
    public void reg(User user) {
        // 根据用户名查询用户数据,判断用户是否被注册过
        User result = userMapper.findByUsername(user.getUsername());
        if (result != null) {
            throw new UsernameDuplicatedException("用户名已被注册");
        }

        // 生成用户ID (这里需要实现一个生成唯一ID的方法)
        user.setUid(generateUniqueId());

        // 密码加密
        user.setPassword(PasswordUtils.hashPassword(user.getPassword()));

        // 补全数据
        user.setIsDelete(0);
        user.setCreatedUser(user.getUsername());
        user.setModifiedUser(user.getUsername());
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedTime(now);
        user.setModifiedTime(now);
        user.setPower("user");
        user.setAvatar("default.jpg");
        user.setGender(0);
        user.setPhone("");
        user.setEmail("");

        // 执行注册
        Integer rows = userMapper.insert(user);
        if (rows != 1) {
            throw new InsertException("注册时发生未知错误");
        }
    }

    // 生成唯一ID的方法
    private Long generateUniqueId() {
        // 使用雪花算法生成ID
        Long id = snowflakeIdGenerator.nextId();

        // 记录到ID生成器表
        try {
            idGeneratorMapper.initIdGenerator("user", id);
        } catch (Exception e) {
            // 如果记录已存在，更新最大ID
            Long currentMaxId = idGeneratorMapper.getCurrentMaxId("user");
            if (currentMaxId != null && id > currentMaxId) {
                idGeneratorMapper.updateMaxId("user", id, 1);
            }
        }

        return id;
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

        // 获取IP和地理位置
        String ip = ipUtil.getIpAddress(request);
        String ipLocation = ipUtil.getIpLocation(ip);

        // 生成token
        String token = jwtUtil.generateAccessToken(result.getUid(), result.getUsername(), ip, ipLocation);

        // 存储到Redis，设置5分钟过期
        String redisKey = "token:" + result.getUid();
        redisUtil.setToken(redisKey, token, 5 * 60 * 1000L);

        // 设置HttpOnly Cookie
        cookieUtil.setTokenCookie(response, token);

        // 存储IP信息，设置7天过期
        redisUtil.setToken("ip:" + result.getUid(), ip + "|" + ipLocation, 7 * 24 * 60 * 60 * 1000L);

        // 设置返回的用户对象
        User user = new User();
        user.setUid(result.getUid());
        user.setUsername(result.getUsername());
        user.setAvatar(result.getAvatar());
        user.setToken("Bearer " + token);
        user.setPhone(result.getPhone());
        user.setEmail(result.getEmail());
        user.setGender(result.getGender());
        user.setPower(result.getPower());

        return user;
    }

    @Override
    public User getUserById(Long uid) {
        User result = userMapper.findByUid(uid);
        if (result == null || result.getIsDelete() == 1) {
            throw new UserNotFoundException("用户不存在");
        }

        // 创建新的User对象，返回完整的用户信息（除了密码）
        User user = new User();
        user.setUid(result.getUid());
        user.setUsername(result.getUsername());
        user.setPower(result.getPower());
        user.setPhone(result.getPhone());
        user.setEmail(result.getEmail());
        user.setGender(result.getGender());
        user.setAvatar(result.getAvatar());
        user.setCreatedTime(result.getCreatedTime());
        user.setModifiedTime(result.getModifiedTime());

        return user;
    }

    @Override
    public User updateUserInfo(User user) {
        // 获取原用户信息
        User result = userMapper.findByUid(user.getUid());
        if (result == null || result.getIsDelete() == 1) {
            throw new UserNotFoundException("用户不存在");
        }

        // 更新用户信息
        User updateUser = new User();
        updateUser.setUid(user.getUid());
        updateUser.setPhone(user.getPhone());
        updateUser.setEmail(user.getEmail());
        updateUser.setGender(user.getGender());
        updateUser.setAvatar(user.getAvatar());
        updateUser.setModifiedUser(result.getUsername());
        updateUser.setModifiedTime(LocalDateTime.now());

        // 执行更新
        Integer rows = userMapper.updateInfo(updateUser);
        if (rows != 1) {
            throw new RuntimeException("更新用户信息时出现未知错误");
        }

        // 获取更新后的用户信息
        return userMapper.findByUid(user.getUid());
    }

    @Override
    public void updatePassword(Long uid, String oldPassword, String newPassword) {
        // 获取用户信息
        User result = userMapper.findByUid(uid);
        if (result == null || result.getIsDelete() == 1) {
            throw new UserNotFoundException("用户不存在");
        }

        // 验证旧密码
        if (!PasswordUtils.checkPassword(oldPassword, result.getPassword())) {
            throw new PasswordNotMatchException("原密码错误");
        }

        // 加密新密码
        String hashedPassword = PasswordUtils.hashPassword(newPassword);

        // 创建更新对象
        User updateUser = new User();
        updateUser.setUid(uid);
        updateUser.setPassword(hashedPassword);
        updateUser.setModifiedUser(result.getUsername());
        updateUser.setModifiedTime(LocalDateTime.now());

        // 执行更新
        Integer rows = userMapper.updatePassword(updateUser);
        if (rows != 1) {
            throw new RuntimeException("更新密码时出现未知错误");
        }
    }
}
