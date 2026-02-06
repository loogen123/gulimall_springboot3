package com.lg.gulimail.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class GulimailSessionConfig {
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        // 1. 必须放大作用域到父域名，否则子域名无法共享登录状态
        serializer.setDomainName("gulimail.com"); 
        serializer.setCookieName("GULISESSION");
        return serializer;
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        // 2. 必须使用 JSON 序列化器，否则 Redis 存的是二进制，乱码且容易报错
        return new GenericJackson2JsonRedisSerializer();
    }
}