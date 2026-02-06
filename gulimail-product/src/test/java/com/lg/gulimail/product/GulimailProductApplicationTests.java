package com.lg.gulimail.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

@SpringBootTest
class GulimailProductApplicationTests {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testStringRedisTemplate() {
        // 1. 获取字符串操作对象
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // 2. 保存数据
        String key = "hello";
        String value = "world_" + UUID.randomUUID().toString();
        ops.set(key, value);
        System.out.println("保存的数据为：" + value);

        // 3. 查询数据
        String result = ops.get(key);
        System.out.println("从Redis中查询到的数据为：" + result);
    }
    @Test
    public void testUpload() throws Exception {

    }
}