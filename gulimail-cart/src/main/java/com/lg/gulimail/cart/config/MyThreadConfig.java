package com.lg.gulimail.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.*;

@Configuration
public class MyThreadConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
                20,           // 核心线程数
                200,          // 最大线程数
                10,           // 存活时间
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(10000), // 队列
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy() // 拒绝策略
        );
    }
}