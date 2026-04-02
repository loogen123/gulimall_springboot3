package com.lg.gulimail.member.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionAndRestConfigTest {

    @Test
    void sessionCookieConfigShouldEnableSecureAttributes() {
        GulimailSessionConfig config = new GulimailSessionConfig();
        DefaultCookieSerializer serializer = (DefaultCookieSerializer) config.cookieSerializer();

        assertEquals("gulimail.com", ReflectionTestUtils.getField(serializer, "domainName"));
        assertEquals("GULISESSION", ReflectionTestUtils.getField(serializer, "cookieName"));
        assertTrue((Boolean) ReflectionTestUtils.getField(serializer, "useHttpOnlyCookie"));
        assertEquals("Lax", ReflectionTestUtils.getField(serializer, "sameSite"));
    }

    @Test
    void restTemplateShouldHaveTimeoutFactory() {
        MemberConfig config = new MemberConfig();
        RestTemplate restTemplate = config.restTemplate();

        SimpleClientHttpRequestFactory requestFactory =
                (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();

        assertEquals(5000, ReflectionTestUtils.getField(requestFactory, "connectTimeout"));
        assertEquals(5000, ReflectionTestUtils.getField(requestFactory, "readTimeout"));
    }
}
