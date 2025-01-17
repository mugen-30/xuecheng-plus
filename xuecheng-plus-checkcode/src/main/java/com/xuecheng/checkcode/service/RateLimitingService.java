package com.xuecheng.checkcode.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitingService {

    private static final long LIMIT = 5; // Limit to 5 requests
    private static final long TIME_WINDOW = 60; // Time window in seconds
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    /**
     * 判断请求是否允许
     *
     * @param key 请求的键，用于标识请求
     * @return 如果请求次数未超过限制，则返回true，否则返回false
     */
    public boolean isAllowed(String key, Long time) {
        key = "rate_limiting:" + key;
        // 对键进行自增操作，并获取自增后的值
        Long count = redisTemplate.opsForValue().increment(key, 1);

        // 如果这是第一次自增（即count为1），则为键设置过期时间
        if (count != null && count == 1) {
            redisTemplate.expire(key, time, TimeUnit.MINUTES);
        }

        // 如果自增后的值小于等于限制值，则返回true，否则返回false
        return count != null && count == 1;
    }

}