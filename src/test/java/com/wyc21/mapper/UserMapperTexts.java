package com.wyc21.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.wyc21.entity.User;

// 测试类 不会随着项目打包一起打包
@SpringBootTest
@SpringJUnitConfig
public class UserMapperTexts {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void insert() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        Integer rows = userMapper.insert(user);
        System.out.println("rows = " + rows);
    }

    @Test
    public void findByUsername() {
        User user = userMapper.findByUsername("test");
        System.out.println("user = " + user);
    }
}
