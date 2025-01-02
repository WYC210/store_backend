package com.wyc21.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SimpleRedisTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testConnection() {
        System.out.println("\n========== 开始测试Redis连接 ==========\n");
        
        try {
            RedisConnection connection = stringRedisTemplate.getConnectionFactory().getConnection();
            
            // 简单的 ping 测试
            String pong = new String(connection.ping());
            System.out.println("Redis响应: " + pong);
            
            // 测试连接是否活跃
            assertTrue(!connection.isClosed(), "连接应该是活跃的");
            System.out.println("连接状态: 活跃");
            
            System.out.println("\n********** Redis连接测试成功！**********\n");
            
        } catch (Exception e) {
            System.out.println("\n********** Redis连接测试失败 **********");
            System.out.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            fail("Redis连接测试失败: " + e.getMessage());
        }
    }

    @Test
    public void testSimpleConnection() {
        System.out.println("\n========== 开始简单Redis测试 ==========\n");
        
        try {
            // 1. 测试设置值
            String key = "hello";
            String value = "world";
            stringRedisTemplate.opsForValue().set(key, value);
            System.out.println("成功设置key: " + key + ", value: " + value);

            // 2. 测试获取值
            String result = stringRedisTemplate.opsForValue().get(key);
            System.out.println("获取到的值: " + result);

            // 3. 验证值是否正确
            assertEquals(value, result, "存储的值应该与获取的值相同");
            System.out.println("值验证成功！");

            // 4. 删除测试数据
            stringRedisTemplate.delete(key);
            System.out.println("测试数据已清理");

            System.out.println("\n********** Redis测试成功！**********\n");
            
        } catch (Exception e) {
            System.out.println("\n********** Redis测试失败 **********");
            System.out.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            fail("Redis测试失败: " + e.getMessage());
        }
    }
} 