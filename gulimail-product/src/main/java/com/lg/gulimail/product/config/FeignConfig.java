package com.lg.gulimail.product.config;

import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {
    
    // 我们需要一个 InheritableThreadLocal 来在主线程和 AI 异步线程之间传递 Cookie，
    // InheritableThreadLocal 可以让子线程自动继承父线程的变量
    public static final InheritableThreadLocal<String> USER_COOKIE_THREAD_LOCAL = new InheritableThreadLocal<>();

    @Bean
    public Request.Options options() {
        // 参数说明：连接超时 5秒，读取超时 10秒
        return new Request.Options(5000, 10000);
    }

    /**
     * Feign 请求拦截器：用于在发起微服务调用时，将原来请求头里的数据（如 Cookie 等）透传过去
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            String aiCookie = USER_COOKIE_THREAD_LOCAL.get();
            if (aiCookie != null && !aiCookie.isEmpty()) {
                template.header("Cookie", aiCookie);
                return;
            }
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            if (request == null) {
                return;
            }
            String cookie = request.getHeader("Cookie");
            if (cookie != null && !cookie.isEmpty()) {
                template.header("Cookie", cookie);
            }
        };
    }
}
