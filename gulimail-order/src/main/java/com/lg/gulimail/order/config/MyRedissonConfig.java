package com.lg.gulimail.order.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedissonConfig {

    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() {
        // 1. 创建配置
        Config config = new Config();
        // 这里地址记得改造成你自己的 Redis 地址
        config.useSingleServer().setAddress("redis://192.168.10.101:6379");

        // 2. 根据 Config 创建出 RedissonClient 实例
        return Redisson.create(config);
    }
}