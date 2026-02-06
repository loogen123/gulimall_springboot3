package com.lg.gulimail.order.config;

import com.lg.gulimail.order.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OrderWebConfiguration implements WebMvcConfigurer {

    @Autowired
    LoginUserInterceptor loginUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/payed/notify",
                        "/order/payed/notify",
                        "/api/order/payed/notify", // 必须有这个，因为你的 notifyUrl 带了 /api
                        "/error",
                        "/image/**",
                        "/favicon.ico"
                );
    }
}