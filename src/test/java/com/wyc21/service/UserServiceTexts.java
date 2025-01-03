package com.wyc21.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.wyc21.entity.User;
import com.wyc21.ShoppingApplication;
import com.wyc21.service.ex.PasswordNotMatchException;
import com.wyc21.service.ex.UserNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShoppingApplication.class)
@SpringJUnitConfig
public class UserServiceTexts {

    @Autowired
    private IUserService userService;

    @Test
    public void testLoginSuccess() {
        System.out.println("\n========== 开始测试登录成功流程 ==========\n");

        // 使用数据库中存在的用户
        String username = "test2";
        String password = "123456";

        try {
            User loginUser = userService.login(username, password);

            // 断言验证
            assertNotNull(loginUser, "登录用户不应为null");
            assertEquals(username, loginUser.getUsername(), "用户名应该匹配");

            System.out.println("\n********** 登录成功！**********");
            System.out.println("登录用户信息：" + loginUser);
            System.out.println("\n========== 测试完成 ==========\n");
        } catch (Exception e) {
            System.out.println("\n********** 测试失败 **********");
            System.out.println("失败原因：" + e.getMessage());
            fail("登录应该成功，但是失败了：" + e.getMessage());
        }
    }

    @Test
    public void testLoginWithWrongPassword() {
        System.out.println("\n========== 开始测试密码错误登录 ==========\n");

        String username = "test2";
        String wrongPassword = "wrong_password"; // 使用明显错误的密码

        Exception exception = assertThrows(PasswordNotMatchException.class, () -> {
            userService.login(username, wrongPassword);
        });

        System.out.println("********** 密码错误测试通过 **********");
        System.out.println("异常信息：" + exception.getMessage());
        System.out.println("\n========== 测试完成 ==========\n");
    }

    @Test
    public void testLoginWithNonexistentUser() {
        System.out.println("\n========== 开始测试用户不存在登录 ==========\n");

        String username = "nonexistent_user";
        String password = "123456";

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.login(username, password);
        });

        System.out.println("********** 用户不存在测试通过 **********");
        System.out.println("异常信息：" + exception.getMessage());
        System.out.println("\n========== 测试完成 ==========\n");
    }

    @Test
    public void testReg() {
        System.out.println("\n========== 开始测试注册流程 ==========\n");

        String username = "test_" + System.currentTimeMillis(); // 使用时间戳确保用户名唯一
        String password = "123456";

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        try {
            userService.reg(user);
            System.out.println("********* 注册成功！*********");

            // 验证注册成功后是否能登录
            User loginUser = userService.login(username, password);
            assertNotNull(loginUser);
            assertEquals(username, loginUser.getUsername());
            System.out.println("********* 注册后登录成功！*********");

        } catch (Exception e) {
            System.out.println("\n********** 测试失败 **********");
            System.out.println("失败原因：" + e.getMessage());
            fail("注册过程发生异常：" + e.getMessage());
        }

        System.out.println("\n========== 测试完成 ==========\n");
    }
}
