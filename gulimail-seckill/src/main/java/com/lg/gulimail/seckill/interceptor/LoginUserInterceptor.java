package com.lg.gulimail.seckill.interceptor;

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

        // 1. 【核心修改】传 false，防止无意义的 Session 创建
        HttpSession session = request.getSession(false);
        MemberResponseVo attribute = null;

        if (session != null) {
            attribute = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        }

        // 2. 如果拿到了用户信息，说明已登录
        if (attribute != null) {
            loginUser.set(attribute);
            return true;
        }

        // 3. 如果没拿到用户信息
        String uri = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();

        // 只有明确要抢购的路径才强制要求登录并创建 Session
        if (antPathMatcher.match("/kill", uri)) {
            // 这里可以执行 getSession()，因为必须登录，此时创建 Session 是合理的
            request.getSession().setAttribute("msg", "请先登录再抢购");
            response.sendRedirect("http://auth.gulimail.com/login.html?originUrl=http://seckill.gulimail.com/kill" + request.getQueryString());
            return false;
        }

        // 4. 其他路径（比如查询秒杀信息的接口）直接放行，既不登录也不创建 Session
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        loginUser.remove();
    }
}