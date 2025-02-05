package com.wyc21.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import jakarta.servlet.http.Cookie;

import com.wyc21.entity.User;
import com.wyc21.ShoppingApplication;
import com.wyc21.service.ex.PasswordNotMatchException;
import com.wyc21.service.ex.UserNotFoundException;
import com.wyc21.util.RedisUtil;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShoppingApplication.class)
@SpringJUnitConfig
public class UserServiceTexts {

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void testLoginSuccess() {
        System.out.println("\n========== 开始测试登录成功流程 ==========\n");

        // 使用数据库中存在的用户
        String username = "test2";
        String password = "123456"; // 这个密码应该是 MD5 加密后的值
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setRemoteAddr("127.0.0.1");

        try {
            User loginUser = userService.login(username, password, request, response);

            // 断言验证
            assertNotNull(loginUser, "登录用户不应为null");
            assertEquals(username, loginUser.getUsername(), "用户名应该匹配");

            System.out.println("\n********** 登录成功！**********");
            System.out.println("登录用户信息：" + loginUser);
            System.out.println("\n========== 测试完成 ==========\n");

            // 验证Cookie是否设置正确
            Cookie[] cookies = response.getCookies();
            assertNotNull(cookies);
            assertTrue(cookies.length > 0);
        } catch (Exception e) {
            e.printStackTrace(); // 添加这行来打印详细错误信息
            System.out.println("\n********** 测试失败 **********");
            System.out.println("失败原因：" + e.getMessage());
            fail("登录应该成功，但是失败了：" + e.getMessage());
        }
    }

    @Test
    public void testLoginWithWrongPassword() {
        System.out.println("\n========== 开始测试密码错误登录 ==========\n");

        String username = "test2";
        String wrongPassword = "wrong_password";
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setRemoteAddr("127.0.0.1");

        Exception exception = assertThrows(PasswordNotMatchException.class, () -> {
            userService.login(username, wrongPassword, request, response);
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
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setRemoteAddr("127.0.0.1");

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.login(username, password, request, response);
        });

        System.out.println("********** 用户不存在测试通过 **********");
        System.out.println("异常信息：" + exception.getMessage());
        System.out.println("\n========== 测试完成 ==========\n");
    }

    @Test
    public void testReg() {
        System.out.println("\n========== 开始测试注册流程 ==========\n");

        String username = "test_" + System.currentTimeMillis();
        String password = "123456";
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setRemoteAddr("127.0.0.1");

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        try {
            userService.reg(user);
            System.out.println("********* 注册成功！*********");

            // 验证注册成功后是否能登录
            User loginUser = userService.login(username, password, request, response);
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

    @Test
    public void testLoginWithDifferentIp() {
        System.out.println("\n========== 开始测试不同IP登录 ==========\n");

        String username = "wz"; // 假设这个用户存在
        String password = "123456"; // 正确的密码
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        request1.setRemoteAddr("192.168.1.1"); // 第一次登录的IP

        // 用户第一次登录
        User loginUser = userService.login(username, password, request1, response1);
        assertNotNull(loginUser, "登录用户不应为null");
        assertEquals(username, loginUser.getUsername(), "用户名应该匹配");

        // 验证 token 是否存储到 Redis
        String redisToken = redisUtil.getToken("token:" + loginUser.getUid());
        assertNotNull(redisToken, "Token 应该存储在 Redis 中");

        // 验证 IP 信息是否存储到 Redis
        String storedIpInfo = redisUtil.getToken("ip:" + loginUser.getUid());
        assertNotNull(storedIpInfo, "IP 信息应该存储在 Redis 中");
        String[] ipParts = storedIpInfo.split("\\|");
        assertEquals("192.168.1.1", ipParts[0], "存储的IP应该匹配");

        // 模拟第二次登录，使用不同的IP
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        request2.setRemoteAddr("192.168.1.2"); // 不同的IP

        // 期望抛出异常
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(username, password, request2, response2);
        });

        // 验证异常信息
        assertEquals("检测到异常登录，请重新登录", exception.getMessage());

        System.out.println("********** 不同IP登录测试通过 **********");
        System.out.println("异常信息：" + exception.getMessage());
        System.out.println("\n========== 测试完成 ==========\n");
    }

    @Test
    public void testUpdateUserInfo() {
        System.out.println("\n========== 开始测试更新用户信息 ==========\n");

        // 准备测试数据
        Long uid = 1L; // 修改为Long类型
        User user = new User();
        user.setUid(uid);
        user.setPhone("13800138000");
        user.setEmail("test@example.com");
        user.setGender(1);
        user.setAvatar("new_avatar.jpg");

        try {
            // 更新用户信息
            User updatedUser = userService.updateUserInfo(user);

            // 验证更新结果
            assertNotNull(updatedUser, "更新后的用户不应为null");
            assertEquals(uid, updatedUser.getUid(), "用户ID应该匹配");
            assertEquals("13800138000", updatedUser.getPhone(), "电话号码应该更新成功");
            assertEquals("test@example.com", updatedUser.getEmail(), "邮箱应该更新成功");
            assertEquals(Integer.valueOf(1), updatedUser.getGender(), "性别应该更新成功");
            assertEquals("new_avatar.jpg", updatedUser.getAvatar(), "头像应该更新成功");

            System.out.println("********** 用户信息更新成功！**********");
            System.out.println("更新后的用户信息：" + updatedUser);

        } catch (Exception e) {
            System.out.println("\n********** 测试失败 **********");
            System.out.println("失败原因：" + e.getMessage());
            fail("更新用户信息应该成功，但是失败了：" + e.getMessage());
        }

        System.out.println("\n========== 测试完成 ==========\n");
    }

    @Test
    public void testUpdateNonexistentUser() {
        System.out.println("\n========== 开始测试更新不存在用户的信息 ==========\n");

        // 准备测试数据
        Long nonexistentUid = 99999L; // 修改为Long类型
        User user = new User();
        user.setUid(nonexistentUid);
        user.setPhone("13800138000");

        // 验证是否抛出UserNotFoundException
        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUserInfo(user);
        });

        System.out.println("********** 测试通过 **********");
        System.out.println("异常信息：" + exception.getMessage());
        System.out.println("\n========== 测试完成 ==========\n");
    }

    @Test
    public void testGetUserInfo() {
        System.out.println("\n========== 开始测试获取用户信息 ==========\n");

        // 准备测试数据
        Long uid = 1L; // 修改为Long类型

        try {
            // 获取用户信息
            User user = userService.getUserById(uid);

            // 验证结果
            assertNotNull(user, "用户不应为null");
            assertEquals(uid, user.getUid(), "用户ID应该匹配");
            assertNotNull(user.getUsername(), "用户名不应为null");
            assertNotNull(user.getPower(), "用户权限不应为null");

            System.out.println("********** 获取用户信息成功！**********");
            System.out.println("用户信息：" + user);

        } catch (Exception e) {
            System.out.println("\n********** 测试失败 **********");
            System.out.println("失败原因：" + e.getMessage());
            fail("获取用户信息应该成功，但是失败了：" + e.getMessage());
        }

        System.out.println("\n========== 测试完成 ==========\n");
    }

    @Test
    public void testGetNonexistentUser() {
        System.out.println("\n========== 开始测试获取不存在用户的信息 ==========\n");

        // 准备测试数据
        Long nonexistentUid = 99999L; // 修改为Long类型

        // 验证是否抛出UserNotFoundException
        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(nonexistentUid);
        });

        System.out.println("********** 测试通过 **********");
        System.out.println("异常信息：" + exception.getMessage());
        System.out.println("\n========== 测试完成 ==========\n");
    }
}
