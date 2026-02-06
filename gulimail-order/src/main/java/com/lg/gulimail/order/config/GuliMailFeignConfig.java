package com.lg.gulimail.order.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class GuliMailFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return template -> {
            // 1. 获取 Spring 上下文中的请求属性
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                // 2. 这里的 request 刚才没定义，现在补上
                HttpServletRequest request = attributes.getRequest();

                // 3. 同步 Cookie
                String cookie = request.getHeader("Cookie");
                template.header("Cookie", cookie);
            } else {
                // 如果你在子线程里直接调 Feign，控制台就会打印下面这行
                System.out.println("⚠️ Feign 拦截器警告：未能获取到 RequestAttributes，Cookie 同步失败！");
            }
        };
    }
}