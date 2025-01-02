package com.wyc21.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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