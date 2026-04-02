package com.lg.gulimail.order.interceptor;

import com.lg.common.constant.AuthServerConstant;
import com.lg.common.vo.MemberResponseVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    private static final Set<String> PAY_NOTIFY_PATHS = Set.of(
            "/payed/notify",
            "/order/payed/notify",
            "/api/order/payed/notify"
    );

    private static final String ORDER_LIST_WITH_ITEM_PATH = "/order/order/listWithItem";

    public static ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        log.debug("order request intercepted: {}", uri);
        HttpSession session = request.getSession(false);
        if (session != null) {
            MemberResponseVo member = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
            if (member != null) {
                loginUser.set(member);
                return true;
            }
        }

        if (isPayNotifyPath(uri)) {
            return true;
        }

        if (isOrderListWithItemPath(uri)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"code\":401, \"msg\":\"未登录\"}");
            return false;
        }

        request.getSession().setAttribute("msg", "请先进行登录");
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
        loginUser.remove();
    }

    private boolean isPayNotifyPath(String uri) {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        for (String payNotifyPath : PAY_NOTIFY_PATHS) {
            if (antPathMatcher.match(payNotifyPath, uri)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOrderListWithItemPath(String uri) {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        return antPathMatcher.match(ORDER_LIST_WITH_ITEM_PATH, uri);
    }
}
