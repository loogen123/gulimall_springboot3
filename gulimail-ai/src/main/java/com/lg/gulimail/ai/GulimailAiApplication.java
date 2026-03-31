package com.lg.gulimail.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.lg.gulimail.ai.mapper")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.lg.gulimail.ai.feign")
@SpringBootApplication
@EnableRedisHttpSession
public class GulimailAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimailAiApplication.class, args);
    }
}
