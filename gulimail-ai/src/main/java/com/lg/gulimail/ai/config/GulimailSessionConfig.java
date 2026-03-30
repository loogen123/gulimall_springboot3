package com.lg.gulimail.ai.config;

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
        // 放大作用域到父域名，解决子域共享
        serializer.setDomainName("gulimail.com");
        serializer.setCookieName("GULISESSION");
        return serializer;
    }

    /**
     * 这里就是你问的 serialize 所在的地方
     * 我们通过匿名内部类的方式，重写 GenericJackson2JsonRedisSerializer 的方法
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer() {
            @Override
            public byte[] serialize(Object t) {
                if (t == null) return new byte[0];

                // --- 🕵️ 侦探逻辑：揪出真凶 ---
                // 只要写入的内容不是 Long (时间戳)，我们看看里面到底存了什么
                if (!(t instanceof Long)) {
                    System.err.println("🚨 【侦测到主动写入】 写入类型: " + t.getClass().getName());
                    System.err.println("📦 写入的具体内容: " + t.toString());

                    // 打印堆栈：看看是哪行代码触发的
                    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                    for (StackTraceElement element : stackTrace) {
                        // 只打印你自己的包名相关的代码
                        if (element.getClassName().contains("com.lg.gulimail")) {
                            System.err.println("👉 触发位置: " + element.getClassName() + "."
                                    + element.getMethodName() + "(行号:" + element.getLineNumber() + ")");
                        }
                    }
                }
                // ---------------------------

                return super.serialize(t);
            }
        };
    }
}