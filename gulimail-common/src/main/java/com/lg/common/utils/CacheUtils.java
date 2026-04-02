package com.lg.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 统一缓存一致性工具类
 * 实现 Cache-Aside 模式：先读缓存，不中则读数据库并回写缓存，写数据库则删除缓存
 */
@Component
public class CacheUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取缓存，若不存在则通过 supplier 获取并缓存
     */
    public <T> T get(String key, Class<T> clazz, Supplier<T> supplier, long timeout, TimeUnit unit) {
        String json = redisTemplate.opsForValue().get(key);
        if (json != null) {
            return com.alibaba.fastjson.JSON.parseObject(json, clazz);
        }

        // 缓存失效，执行逻辑获取数据
        T data = supplier.get();
        if (data != null) {
            redisTemplate.opsForValue().set(key, com.alibaba.fastjson.JSON.toJSONString(data), timeout, unit);
        }
        return data;
    }

    /**
     * 删除缓存（更新数据时调用）
     */
    public void evict(String key) {
        redisTemplate.delete(key);
    }
}
