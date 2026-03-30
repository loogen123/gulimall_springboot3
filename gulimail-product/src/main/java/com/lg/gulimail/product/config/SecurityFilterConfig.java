package com.lg.gulimail.product.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;

// 注释掉这个会导致 Session 丢失的自定义过滤器
// @Configuration
public class SecurityFilterConfig {

//    @Bean
//    @Order(Ordered.HIGHEST_PRECEDENCE)
//    public Filter ultimateSecurityFilter() {
//        return new Filter() {
//            @Override
//            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//                    throws IOException, ServletException {
//                HttpServletRequest req = (HttpServletRequest) request;
//                HttpServletResponse res = (HttpServletResponse) response;
//                String uri = req.getRequestURI();
//
//                // 1. 针对垃圾请求：直接原地打死 (保持你原有的逻辑)
//                if (uri.endsWith(".map") || uri.contains("favicon.ico")) {
//                    res.setStatus(404);
//                    return;
//                }
//
//                // 2. 针对详情页主请求：包装 Request，严禁创建 Session
//                // 只有这样，Thymeleaf 渲染到 writeContent 时，getSession(true) 才会返回 null
//                if (uri.endsWith(".html") || uri.contains("/item/")) {
//                    HttpServletRequestWrapper noSessionWrapper = new HttpServletRequestWrapper(req) {
//                        @Override
//                        public HttpSession getSession(boolean create) {
//                            if (create) return null; // 强行拒绝创建
//                            return super.getSession(false);
//                        }
//                        @Override
//                        public HttpSession getSession() {
//                            return null; // 默认不给
//                        }
//                    };
//                    chain.doFilter(noSessionWrapper, response);
//                } else {
//                    // 3. 其他正常请求（如登录、购物车）正常放行
//                    chain.doFilter(request, response);
//                }
//            }
//        };
//    }
}