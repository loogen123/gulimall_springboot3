package com.lg.gulimail.member.interceptor;

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
    void preHandleShouldPassWhenLoggedIn() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/member/member/list");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        MemberResponseVo member = new MemberResponseVo();
        member.setId(7L);
        session.setAttribute(AuthServerConstant.LOGIN_USER, member);
        request.setSession(session);

        boolean pass = interceptor.preHandle(request, response, new Object());

        assertTrue(pass);
        assertNotNull(LoginUserInterceptor.loginUser.get());
        assertEquals(7L, LoginUserInterceptor.loginUser.get().getId());
    }

    @Test
    void preHandleShouldRedirectWithEncodedOriginUrlWhenNotLoggedIn() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/member/member/list");
        request.setQueryString("pageNum=2&from=center+menu");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean pass = interceptor.preHandle(request, response, new Object());

        assertFalse(pass);
        assertEquals(
                "http://auth.gulimail.com/login.html?originUrl=http%3A%2F%2Flocalhost%2Fmember%2Fmember%2Flist%3FpageNum%3D2%26from%3Dcenter%2Bmenu",
                response.getRedirectedUrl()
        );
    }

    @Test
    void preHandleShouldPassForInternalPathWithoutLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/member/member/internal/integration/quote");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean pass = interceptor.preHandle(request, response, new Object());

        assertTrue(pass);
        assertNull(response.getRedirectedUrl());
    }

    @Test
    void afterCompletionShouldClearThreadLocal() {
        LoginUserInterceptor.loginUser.set(new MemberResponseVo());

        interceptor.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), null);

        assertNull(LoginUserInterceptor.loginUser.get());
    }
}
