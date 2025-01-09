package com.xuecheng.checkcode.service.impl;

import com.xuecheng.checkcode.service.CheckCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author Mr.M
 * @version 1.0
 * @description 使用redis存储验证码，测试用
 * @date 2022/9/29 18:36
 */
@Component("RedisCheckCodeStore")
@Slf4j
public class RedisCheckCodeStore implements CheckCodeService.CheckCodeStore {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public void set(String key, String value, Integer expire) {
        log.debug("验证码key:{},value:{}", key, value);

        try {
            stringRedisTemplate.opsForValue().set(key,value,expire, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String get(String key) {
        return (String) stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void remove(String key) {
        stringRedisTemplate.delete(key);
    }
}
