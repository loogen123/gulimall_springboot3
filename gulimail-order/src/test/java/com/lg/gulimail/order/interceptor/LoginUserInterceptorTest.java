package com.lg.gulimail.order.interceptor;

import com.lg.common.constant.AuthServerConstant;
import com.lg.common.vo.MemberResponseVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginUserInterceptorTest {

    private final LoginUserInterceptor interceptor = new LoginUserInterceptor();

    @AfterEach
    void clearThreadLocal() {
        LoginUserInterceptor.loginUser.remove();
    }

    @Test
    void preHandleShouldPassWhenSessionContainsLoginUser() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/order/order/listWithItem");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        MemberResponseVo member = new MemberResponseVo();
        member.setId(1L);
        session.setAttribute(AuthServerConstant.LOGIN_USER, member);
        request.setSession(session);

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        assertNotNull(LoginUserInterceptor.loginUser.get());
        assertEquals(1L, LoginUserInterceptor.loginUser.get().getId());
    }

    @Test
    void preHandleShouldReturnUnauthorizedForListWithItemWhenNoLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/order/order/listWithItem");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandleShouldAllowPayNotifyPathWhenNoLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/order/payed/notify");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
    }

    @Test
    void preHandleShouldRedirectForNormalProtectedPathWhenNoLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/order/order/list");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertNotNull(response.getRedirectedUrl());
        assertTrue(response.getRedirectedUrl().startsWith("http://auth.gulimail.com/login.html"));
    }

    @Test
    void preHandleShouldEncodeOriginUrlWithQueryStringWhenRedirect() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/order/order/list");
        request.setQueryString("orderSn=123&source=cart+page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(
                "http://auth.gulimail.com/login.html?originUrl=http%3A%2F%2Flocalhost%2Forder%2Forder%2Flist%3ForderSn%3D123%26source%3Dcart%2Bpage",
                response.getRedirectedUrl()
        );
    }

    @Test
    void afterCompletionShouldClearThreadLocal() throws Exception {
        MemberResponseVo member = new MemberResponseVo();
        member.setId(2L);
        LoginUserInterceptor.loginUser.set(member);
        interceptor.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), null);
        assertNull(LoginUserInterceptor.loginUser.get());
    }
}
