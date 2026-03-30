package com.lg.gulimail.order.interceptor;

import com.lg.common.constant.AuthServerConstant;
import com.lg.common.vo.MemberResponseVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 获取当前请求的 URI
        String uri = request.getRequestURI();
        
        // 2. 正常的登录逻辑检查
        HttpSession session = request.getSession();
        System.out.println("【订单服务拦截器】当前请求 URI: " + uri);
        System.out.println("【订单服务拦截器】当前 Session ID: " + session.getId());
        
        MemberResponseVo attribute = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);

        if (attribute != null) {
            System.out.println("【订单服务拦截器】成功获取到用户信息，用户名: " + attribute.getNickname());
            // 已登录，存入 ThreadLocal
            loginUser.set(attribute);
            return true;
        }

        // 3. 核心：强制放行支付回调请求
        // 支付宝通知可能会匹配多种路径，只要包含 payed 或 notify 字样，直接放行，不检查 Session
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match1 = antPathMatcher.match("/payed/notify", uri);
        boolean match2 = antPathMatcher.match("/order/payed/notify", uri);
        boolean match3 = antPathMatcher.match("/api/order/payed/notify", uri);
        
        if (match1 || match2 || match3 || uri.contains("payed")) {
            return true;
        }

        // 4. 对于内部 Feign 调用的 API，如果未登录，直接返回 401 状态码而不是重定向
        boolean match4 = antPathMatcher.match("/order/order/listWithItem", uri);
        if (match4) {
            System.out.println("【订单服务拦截器】内部Feign调用未获取到用户信息，直接拦截并返回 401");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401, \"msg\":\"未登录\"}");
            return false;
        }

        System.out.println("【订单服务拦截器】未能从 Session 中获取到用户信息，拦截请求并重定向。");
        // 未登录，保存当前尝试访问的地址，并重定向到登录页
        request.getSession().setAttribute("msg", "请先进行登录");
        response.sendRedirect("http://auth.gulimail.com/login.html?originUrl=" + request.getRequestURL());
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清理 ThreadLocal，防止内存泄漏
        loginUser.remove();
    }
}