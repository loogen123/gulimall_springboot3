package com.lg.gulimail.cart.config;

import org.junit.jupiter.api.Test;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartConfigTest {

    @Test
    void sessionCookieConfigShouldContainSecurityFlags() {
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
    void sessionCookieConfigShouldDisableSecureByDefault() {
        GulimailSessionConfig config = new GulimailSessionConfig();
        ReflectionTestUtils.setField(config, "secureCookie", false);
        DefaultCookieSerializer serializer = (DefaultCookieSerializer) config.cookieSerializer();

        assertFalse((Boolean) ReflectionTestUtils.getField(serializer, "useSecureCookie"));
    }

    @Test
    void threadPoolConfigShouldReadConfiguredFields() {
        MyThreadConfig config = new MyThreadConfig();
        ReflectionTestUtils.setField(config, "coreSize", 8);
        ReflectionTestUtils.setField(config, "maxSize", 16);
        ReflectionTestUtils.setField(config, "keepAliveSeconds", 20);
        ReflectionTestUtils.setField(config, "queueCapacity", 128);

        ThreadPoolExecutor executor = config.threadPoolExecutor();

        assertEquals(8, executor.getCorePoolSize());
        assertEquals(16, executor.getMaximumPoolSize());
        assertEquals(20, executor.getKeepAliveTime(java.util.concurrent.TimeUnit.SECONDS));
        assertEquals(128, executor.getQueue().remainingCapacity());
        executor.shutdownNow();
    }
}
