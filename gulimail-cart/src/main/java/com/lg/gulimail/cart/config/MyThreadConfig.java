package com.lg.gulimail.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.*;

@Configuration
public class MyThreadConfig {
    @Value("${gulimail.cart.thread.core-size:16}")
    private int coreSize;

    @Value("${gulimail.cart.thread.max-size:64}")
    private int maxSize;

    @Value("${gulimail.cart.thread.keep-alive-seconds:30}")
    private int keepAliveSeconds;

    @Value("${gulimail.cart.thread.queue-capacity:2000}")
    private int queueCapacity;

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
                coreSize,
                maxSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(queueCapacity),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
