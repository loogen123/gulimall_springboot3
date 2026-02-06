package com.lg.gulimail.product.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class) // 关键：让 CacheProperties 生效
public class MyCacheConfig {

    /**
     * 自定义 Redis 缓存配置
     * 1. 序列化机制改为 JSON
     * 2. 读取配置文件中的过期时间等参数
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();

        // 【核心】1. 设置 Key 和 Value 的序列化方式
        config = config.serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
        );
        config = config.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
        );

        // 【关键】2. 让配置文件中的配置项生效（如果不写，yml 里的设置会被这里覆盖失效）
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();

        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive()); // 设置过期时间
        }
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixCacheNameWith(redisProperties.getKeyPrefix()); // 设置 Key 前缀
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues(); // 禁止缓存空值
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix(); // 禁用前缀
        }

        return config;
    }
}