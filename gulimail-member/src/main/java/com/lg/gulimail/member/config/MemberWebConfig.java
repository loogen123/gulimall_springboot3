package com.lg.gulimail.member.config;

import com.lg.gulimail.member.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MemberWebConfig implements WebMvcConfigurer {

    @Autowired
    LoginUserInterceptor loginUserInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/member/member/oauth2/login")
                .excludePathPatterns("/member/member/login")
                .excludePathPatterns("/member/member/register")
                .excludePathPatterns("/member/member/internal/**");
    }
}
