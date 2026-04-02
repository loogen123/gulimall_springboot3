package com.lg.gulimail.search.config;

import org.junit.jupiter.api.Test;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchSessionConfigTest {

    @Test
    void cookieSerializerShouldContainSecurityFlags() {
        GulimailSessionConfig config = new GulimailSessionConfig();
        ReflectionTestUtils.setField(config, "secureCookie", true);
        DefaultCookieSerializer serializer = (DefaultCookieSerializer) config.cookieSerializer();

        assertEquals("gulimail.com", ReflectionTestUtils.getField(serializer, "domainName"));
        assertEquals("GULISESSION", ReflectionTestUtils.getField(serializer, "cookieName"));
        assertTrue((Boolean) ReflectionTestUtils.getField(serializer, "useHttpOnlyCookie"));
        assertEquals("Lax", ReflectionTestUtils.getField(serializer, "sameSite"));
        assertTrue((Boolean) ReflectionTestUtils.getField(serializer, "useSecureCookie"));
    }

    @Test
    void cookieSerializerShouldDisableSecureByDefault() {
        GulimailSessionConfig config = new GulimailSessionConfig();
        ReflectionTestUtils.setField(config, "secureCookie", false);
        DefaultCookieSerializer serializer = (DefaultCookieSerializer) config.cookieSerializer();

        assertFalse((Boolean) ReflectionTestUtils.getField(serializer, "useSecureCookie"));
    }
}
