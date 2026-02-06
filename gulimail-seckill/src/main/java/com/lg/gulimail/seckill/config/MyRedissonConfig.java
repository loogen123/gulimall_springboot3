package com.lg.gulimail.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {

    /**
     * 所有对 Redisson 的使用都是通过 RedissonClient 对象
     */
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        // 1. 创建配置
        Config config = new Config();
        // Redis 地址需要以 redis:// 开头，如果是 SSL 则用 rediss://
        // 这里记得换成你自己的虚拟机或服务器 IP
        config.useSingleServer().setAddress("redis://192.168.10.101:6379");

        // 2. 根据 Config 创建出 RedissonClient 实例
        return Redisson.create(config);
    }
}