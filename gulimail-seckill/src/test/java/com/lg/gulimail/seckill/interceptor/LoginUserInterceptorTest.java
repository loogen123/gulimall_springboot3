package com.lg.gulimail.seckill.interceptor;

import com.lg.common.exception.BizCodeEnum;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginUserInterceptorTest {

    @Test
    void preHandleShouldRedirectToEncodedOriginWhenKillPathAndQueryPresent() throws Exception {
        LoginUserInterceptor interceptor = new LoginUserInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/kill");
        request.setQueryString("killId=1_2&key=a+b");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(
                "http://auth.gulimail.com/login.html?originUrl=http%3A%2F%2Fseckill.gulimail.com%2Fkill%3FkillId%3D1_2%26key%3Da%2Bb",
                response.getRedirectedUrl()
        );
        HttpSession session = request.getSession(false);
        assertTrue(session != null && BizCodeEnum.UNAUTHORIZED_EXCEPTION.getMsg().equals(session.getAttribute("msg")));
    }

    @Test
    void preHandleShouldRedirectWhenKillPathAndQueryMissing() throws Exception {
        LoginUserInterceptor interceptor = new LoginUserInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/kill");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(
                "http://auth.gulimail.com/login.html?originUrl=http%3A%2F%2Fseckill.gulimail.com%2Fkill",
                response.getRedirectedUrl()
        );
    }
}
