package com.lg.gulimail.member.interceptor;

import com.lg.common.constant.AuthServerConstant;
import com.lg.common.vo.MemberResponseVo;
import jakarta.servlet.http.HttpServletRequest;  // 必须是 jakarta
import jakarta.servlet.http.HttpServletResponse; // 必须是 jakarta
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LoginUserInterceptor.class);
    public static final ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getRequestURI().startsWith("/member/member/internal/")) {
            return true;
        }
        Object attribute = null;
        if (request.getSession(false) != null) {
            attribute = request.getSession(false).getAttribute(AuthServerConstant.LOGIN_USER);
        }

        if (attribute != null) {
            loginUser.set((MemberResponseVo) attribute);
            return true;
        } else {
            log.warn("未登录访问受限路径: {}", request.getRequestURI());
            String originUrl = request.getRequestURL().toString();
            String queryString = request.getQueryString();
            if (queryString != null && !queryString.isBlank()) {
                originUrl = originUrl + "?" + queryString;
            }
            String encodedOriginUrl = URLEncoder.encode(originUrl, StandardCharsets.UTF_8);
            response.sendRedirect("http://auth.gulimail.com/login.html?originUrl=" + encodedOriginUrl);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        loginUser.remove();
    }
}
