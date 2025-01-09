package com.xuecheng.checkcode;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
public class testRedis {
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Test
    void test(){
//        stringRedisTemplate.opsForValue().set("key","value");
        System.out.println(stringRedisTemplate.opsForValue().get("key"));
    }
}
