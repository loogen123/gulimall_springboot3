package com.lg.gulimail.ai.config;

import org.junit.jupiter.api.Test;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiSessionConfigTest {

    @Test
    void cookieSerializerShouldContainSecurityFlags() {
        GulimailSessionConfig config = new GulimailSessionConfig();
        DefaultCookieSerializer serializer = (DefaultCookieSerializer) config.cookieSerializer();

        assertEquals("gulimail.com", ReflectionTestUtils.getField(serializer, "domainName"));
        assertEquals("GULISESSION", ReflectionTestUtils.getField(serializer, "cookieName"));
        assertTrue((Boolean) ReflectionTestUtils.getField(serializer, "useHttpOnlyCookie"));
        assertEquals("Lax", ReflectionTestUtils.getField(serializer, "sameSite"));
    }
}
