package com.lg.gulimail.product.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FeignConfigTest {

    private final FeignConfig feignConfig = new FeignConfig();

    @AfterEach
    void cleanup() {
        FeignConfig.USER_COOKIE_THREAD_LOCAL.remove();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldUseThreadLocalCookieWhenPresent() {
        FeignConfig.USER_COOKIE_THREAD_LOCAL.set("SESSION=abc");
        RequestInterceptor interceptor = feignConfig.requestInterceptor();
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertEquals("SESSION=abc", getCookieHeader(template));
    }

    @Test
    void shouldUseRequestHeaderCookieWhenThreadLocalEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Cookie", "SESSION=from-request");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        RequestInterceptor interceptor = feignConfig.requestInterceptor();
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertEquals("SESSION=from-request", getCookieHeader(template));
    }

    @Test
    void shouldNotSetCookieWhenNoContextAndNoThreadLocal() {
        RequestInterceptor interceptor = feignConfig.requestInterceptor();
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertNull(getCookieHeader(template));
    }

    private String getCookieHeader(RequestTemplate template) {
        Map<String, Collection<String>> headers = template.headers();
        Collection<String> cookies = headers.get("Cookie");
        if (cookies == null || cookies.isEmpty()) {
            return null;
        }
        return cookies.iterator().next();
    }
}
