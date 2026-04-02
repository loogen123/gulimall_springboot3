package com.lg.gulimail.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;

@Configuration
@EnableConfigurationProperties(GuliMailCorsProperties.class)
public class GuliMailCorsConfiguration {

    private final GuliMailCorsProperties corsProperties;

    public GuliMailCorsConfiguration(GuliMailCorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public CorsWebFilter corsWebFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        List<String> allowedHeaders = corsProperties.getAllowedHeaders() == null ? List.of("*") : corsProperties.getAllowedHeaders();
        List<String> allowedMethods = corsProperties.getAllowedMethods() == null ? List.of("GET", "POST", "OPTIONS") : corsProperties.getAllowedMethods();
        List<String> allowedOriginPatterns = corsProperties.getAllowedOriginPatterns() == null ? List.of() : corsProperties.getAllowedOriginPatterns();
        List<String> exposedHeaders = corsProperties.getExposedHeaders() == null ? List.of() : corsProperties.getExposedHeaders();
        allowedHeaders.forEach(corsConfiguration::addAllowedHeader);
        allowedMethods.forEach(corsConfiguration::addAllowedMethod);
        allowedOriginPatterns.forEach(corsConfiguration::addAllowedOriginPattern);
        exposedHeaders.forEach(corsConfiguration::addExposedHeader);
        corsConfiguration.setAllowCredentials(corsProperties.isAllowCredentials());
        corsConfiguration.setMaxAge(Math.max(0L, corsProperties.getMaxAgeSeconds()));

        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(source);
    }
}
