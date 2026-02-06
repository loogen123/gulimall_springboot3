package com.lg.gulimail.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.*;

@Configuration
public class MyThreadConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        /**
         * 参数说明：
         * 20: 核心线程数
         * 200: 最大线程数
         * 10: 空闲等待时间
         * SECONDS: 时间单位
         * LinkedBlockingQueue: 等待队列（记得给大小，防止 OOM）
         * Executors.defaultThreadFactory(): 线程工厂
         * ThreadPoolExecutor.AbortPolicy(): 拒绝策略
         */
        return new ThreadPoolExecutor(
                20, 
                200, 
                10, 
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}