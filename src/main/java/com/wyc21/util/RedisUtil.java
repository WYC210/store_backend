package com.wyc21.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 存储字符串数据到Redis，并设置过期时间（毫秒）
     */
    public void setWithExpire(String key, String value, long timeoutMillis) {
        stringRedisTemplate.opsForValue().set(key, value, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取字符串数据
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除数据
     */
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 检查key是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void setToken(String key, String token, long timeout) {
        stringRedisTemplate.opsForValue().set(key, token, timeout, TimeUnit.MILLISECONDS);
    }

    public String getToken(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void deleteToken(String key) {
        stringRedisTemplate.delete(key);
    }
}