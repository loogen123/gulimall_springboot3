package com.lg.gulimail.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class GulimailSessionConfig {
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        System.out.println("✅ 确认：自定义 JSON 序列化器已成功加载！");
        // 使用 Jackson2JsonRedisSerializer 或者 GenericJackson2JsonRedisSerializer
        return new GenericJackson2JsonRedisSerializer();
    }
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setDomainName("gulimail.com"); // 必须有这一行！
        serializer.setCookieName("GULISESSION");
        return serializer;
    }
}