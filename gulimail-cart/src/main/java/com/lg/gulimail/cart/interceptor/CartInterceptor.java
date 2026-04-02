package com.lg.gulimail.cart.interceptor;

import com.lg.common.constant.AuthServerConstant;
import com.lg.common.vo.MemberResponseVo;
import com.lg.gulimail.cart.to.UserInfoTo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        MemberResponseVo user = session == null ? null : (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);

        if (user != null) {
            UserInfoTo userInfoTo = new UserInfoTo();
            userInfoTo.setUserId(user.getId());
            threadLocal.set(userInfoTo);
            return true;
        }
        request.getSession().setAttribute("msg", "购物车功能需登录后使用");
        String originUrl = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isBlank()) {
            originUrl = originUrl + "?" + queryString;
        }
        String encodedOriginUrl = URLEncoder.encode(originUrl, StandardCharsets.UTF_8);
        response.sendRedirect("http://auth.gulimail.com/login.html?originUrl=" + encodedOriginUrl);
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        threadLocal.remove();
    }
}
