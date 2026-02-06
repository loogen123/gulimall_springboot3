package com.lg.gulimail.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        // 1. 设置连接池基本参数
        poolConfig.setMaxTotal(200);
        poolConfig.setMaxIdle(50);
        poolConfig.setMinIdle(10);

        // 2. 关键：手动关闭 JMX 注册，防止 MBean 名称冲突导致启动失败
        poolConfig.setJmxEnabled(false);

        // 3. 创建并返回
        return new JedisPool(poolConfig, host, port);
    }
}