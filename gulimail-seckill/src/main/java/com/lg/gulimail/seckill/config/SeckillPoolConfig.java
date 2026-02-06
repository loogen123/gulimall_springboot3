package com.lg.gulimail.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class SeckillPoolConfig {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);    // 核心线程
        executor.setMaxPoolSize(200);   // 最大线程
        executor.setQueueCapacity(500); // 队列
        executor.setThreadNamePrefix("seckill-async-");
        executor.initialize();
        return executor;
    }
}