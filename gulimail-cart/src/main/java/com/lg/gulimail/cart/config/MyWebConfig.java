package com.lg.gulimail.cart.config;

import com.lg.gulimail.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截所有请求，确保每个进入购物车的请求都能被识别用户身份
        registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
    }
}