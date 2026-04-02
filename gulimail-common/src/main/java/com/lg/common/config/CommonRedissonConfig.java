package com.lg.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * 公共 Redisson 配置
 */
@Configuration
@AutoConfigureBefore(name = "org.redisson.spring.starter.RedissonAutoConfiguration")
@ConditionalOnProperty(prefix = "spring.data.redis", name = "host")
public class CommonRedissonConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private String port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.ssl:false}")
    private boolean ssl;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() throws IOException {
        Config config = new Config();
        String prefix = ssl ? "rediss://" : "redis://";
        String address = prefix + host + ":" + port;
        
        config.useSingleServer()
                .setAddress(address)
                .setPassword(password.isEmpty() ? null : password)
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(20)
                .setRetryAttempts(3)
                .setRetryInterval(1000)
                .setPingConnectionInterval(30000);

        // 设置看门狗时间，默认 30s
        config.setLockWatchdogTimeout(30000);

        return Redisson.create(config);
    }
}
