package com.lg.gulimail.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SentinelGatewayConfigTest {

    @Test
    void initShouldFallbackTo429WhenBlockCodeInvalid() {
        SentinelGatewayConfig config = new SentinelGatewayConfig();
        ReflectionTestUtils.setField(config, "blockCode", 999);
        ReflectionTestUtils.setField(config, "blockMsg", "系统繁忙");

        config.init();

        Object responseCode = ReflectionTestUtils.invokeMethod(config, "resolveResponseCode", 999);
        assertEquals(429, responseCode);
    }

    @Test
    void escapeJsonShouldHandleNull() {
        SentinelGatewayConfig config = new SentinelGatewayConfig();

        Object body = ReflectionTestUtils.invokeMethod(config, "escapeJson", new Object[]{null});

        assertEquals("", body);
    }

    @Test
    void buildResponseBodyShouldEscapeMessageAndUseBizCode() {
        SentinelGatewayConfig config = new SentinelGatewayConfig();

        Object responseBody = ReflectionTestUtils.invokeMethod(config, "buildResponseBody", 10004, "a\\\"b");

        assertEquals("{\"code\":10004,\"msg\":\"a\\\\\\\"b\"}", responseBody);
    }
}
