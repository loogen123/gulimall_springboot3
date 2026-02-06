package com.lg.gulimail.cart.interceptor;

import com.lg.common.vo.MemberResponseVo;
import com.lg.gulimail.cart.to.UserInfoTo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();
        MemberResponseVo user = (MemberResponseVo) session.getAttribute("loginUser");

        if (user != null) {
            // 1. 已登录：封装用户信息到 UserInfoTo 供 Service 使用
            UserInfoTo userInfoTo = new UserInfoTo();
            userInfoTo.setUserId(user.getId());

            threadLocal.set(userInfoTo);
            return true;
        } else {
            // 2. 未登录：拦截并跳转到认证中心
            session.setAttribute("msg", "购物车功能需登录后使用");
            response.sendRedirect("http://auth.gulimail.com/login.html?originUrl=" + request.getRequestURL());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束，一定要清理，否则线程池复用会导致用户信息串位
        threadLocal.remove();
    }
}