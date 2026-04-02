package com.lg.gulimail.order.config;

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

class GuliMailFeignConfigTest {

    private final GuliMailFeignConfig config = new GuliMailFeignConfig();

    @AfterEach
    void cleanup() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void requestInterceptorShouldSetCookieWhenRequestPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Cookie", "GULISESSION=abc");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        RequestInterceptor interceptor = config.requestInterceptor();
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertEquals("GULISESSION=abc", getCookieHeader(template));
    }

    @Test
    void requestInterceptorShouldSkipWhenNoRequestContext() {
        RequestInterceptor interceptor = config.requestInterceptor();
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
