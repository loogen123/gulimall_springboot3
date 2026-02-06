package com.lg.gulimail.product.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionDebugInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 这种方式不会创建 Session，只检查有没有已存在的
        HttpSession session = request.getSession(false);
        if (session != null) {
            System.out.println("检测到请求路径：" + request.getRequestURI() + " 已持有 Session ID: " + session.getId());
        }
        return true;
    }
}