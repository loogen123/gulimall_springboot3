package com.lg.gulimail.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser; // 引入 PathPatternParser

@Configuration
public class GuliMailCorsConfiguration {
    @Bean
    public CorsWebFilter corsWebFilter(){
        // 关键改动 1: 传入 PathPatternParser，确保 WebFlux 环境下路径匹配正确
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 1.配置跨域
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");

        // ***************************************************************
        // ** 关键改动 2: 使用 addAllowedOriginPattern("*") 解决 500 异常 **
        // ***************************************************************
        // 允许携带凭证(true)时，必须使用 Pattern 模式来允许通配符，
        // Spring 会在运行时将其替换为准确的 Origin（http://localhost:8001）
        corsConfiguration.addAllowedOriginPattern("*");

        corsConfiguration.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(source);
    }
}