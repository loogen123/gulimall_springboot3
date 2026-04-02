package com.lg.gulimail.cart.interceptor;

import com.lg.common.constant.AuthServerConstant;
import com.lg.common.vo.MemberResponseVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartInterceptorTest {

    private final CartInterceptor cartInterceptor = new CartInterceptor();

    @AfterEach
    void clearThreadLocal() {
        CartInterceptor.threadLocal.remove();
    }

    @Test
    void preHandleShouldPassWhenLoggedIn() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/cartList.html");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        MemberResponseVo member = new MemberResponseVo();
        member.setId(101L);
        session.setAttribute(AuthServerConstant.LOGIN_USER, member);
        request.setSession(session);

        boolean pass = cartInterceptor.preHandle(request, response, new Object());

        assertTrue(pass);
        assertEquals(101L, CartInterceptor.threadLocal.get().getUserId());
    }

    @Test
    void preHandleShouldRedirectWhenNotLoggedIn() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/cartList.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean pass = cartInterceptor.preHandle(request, response, new Object());

        assertFalse(pass);
        assertTrue(response.getRedirectedUrl().startsWith("http://auth.gulimail.com/login.html"));
    }

    @Test
    void preHandleShouldEncodeOriginUrlWithQueryStringWhenRedirect() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/cartList.html");
        request.setQueryString("skuId=1&from=cart+page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean pass = cartInterceptor.preHandle(request, response, new Object());

        assertFalse(pass);
        assertEquals(
                "http://auth.gulimail.com/login.html?originUrl=http%3A%2F%2Flocalhost%2FcartList.html%3FskuId%3D1%26from%3Dcart%2Bpage",
                response.getRedirectedUrl()
        );
    }

    @Test
    void afterCompletionShouldClearThreadLocal() throws Exception {
        CartInterceptor.threadLocal.set(new com.lg.gulimail.cart.to.UserInfoTo());
        cartInterceptor.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), null);
        assertNull(CartInterceptor.threadLocal.get());
    }
}
