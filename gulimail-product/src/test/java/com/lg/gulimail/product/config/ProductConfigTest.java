package com.lg.gulimail.product.config;

import org.junit.jupiter.api.Test;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductConfigTest {

    @Test
    void sessionCookieShouldContainSecurityFlags() {
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
    void sessionCookieShouldDisableSecureByDefault() {
        GulimailSessionConfig config = new GulimailSessionConfig();
        ReflectionTestUtils.setField(config, "secureCookie", false);
        DefaultCookieSerializer serializer = (DefaultCookieSerializer) config.cookieSerializer();

        assertFalse((Boolean) ReflectionTestUtils.getField(serializer, "useSecureCookie"));
    }

    @Test
    void threadPoolShouldUseConfiguredQueueCapacity() {
        ThreadPoolConfigProperties properties = new ThreadPoolConfigProperties();
        properties.setCoreSize(4);
        properties.setMaxSize(8);
        properties.setKeepAliveTime(12);
        properties.setQueueCapacity(256);
        MyThreadConfig config = new MyThreadConfig();
        ReflectionTestUtils.setField(config, "pool", properties);

        ThreadPoolExecutor executor = config.threadPoolExecutor();

        assertEquals(4, executor.getCorePoolSize());
        assertEquals(8, executor.getMaximumPoolSize());
        assertEquals(12, executor.getKeepAliveTime(TimeUnit.SECONDS));
        assertEquals(256, executor.getQueue().remainingCapacity());
        executor.shutdownNow();
    }
}
