package com.lg.gulimail.product.config;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public Request.Options options() {
        // 参数说明：连接超时 5秒，读取超时 10秒
        return new Request.Options(5000, 10000);
    }
}