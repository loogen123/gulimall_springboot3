package com.lg.gulimail.member.interceptor;

import com.lg.common.constant.AuthServerConstant;
import com.lg.common.vo.MemberResponseVo;
import jakarta.servlet.http.HttpServletRequest;  // 必须是 jakarta
import jakarta.servlet.http.HttpServletResponse; // 必须是 jakarta
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 尝试获取 Session
        Object attribute = request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);

        if (attribute != null) {
            loginUser.set((MemberResponseVo) attribute);
            return true;
        } else {
            // 还是被拦了？说明 Session 没同步过来
            System.out.println("拦截器拦截了路径：" + request.getRequestURI());
            response.sendRedirect("http://auth.gulimail.com/login.html");
            return false;
        }
    }
}