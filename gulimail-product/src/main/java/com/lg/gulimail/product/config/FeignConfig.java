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
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 1. 先尝试从全局 ThreadLocal 中获取（适配 AI 异步线程）
                String aiCookie = USER_COOKIE_THREAD_LOCAL.get();
                if (aiCookie != null && !aiCookie.isEmpty()) {
                    System.out.println("【Feign请求拦截器】从 AI 异步线程获取到 Cookie: " + aiCookie);
                    template.header("Cookie", aiCookie);
                    return;
                }

                // 2. 如果没获取到，说明是普通的同步请求，直接从 RequestContextHolder 获取
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    if (request != null) {
                        // 优先尝试从请求头获取 Cookie
                        String cookie = request.getHeader("Cookie");
                        if (cookie != null) {
                            System.out.println("【Feign请求拦截器】从主线程获取到 Cookie: " + cookie);
                            template.header("Cookie", cookie);
                        } else {
                            System.out.println("【Feign请求拦截器】主线程请求中没有 Cookie。");
                        }
                    }
                } else {
                    System.out.println("【Feign请求拦截器】未获取到请求上下文，且 ThreadLocal 为空。");
                    // 以前为了 AI 做的终极兜底逻辑，现在 AI 已经迁移到独立模块，这里直接删掉兜底逻辑
                }
            }
        };
    }
}