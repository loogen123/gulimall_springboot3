package com.lg.gulimail.gateway.filter;

import com.lg.gulimail.gateway.config.GatewaySecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplayProtectFilterTest {

    @Test
    void shouldRejectWhenRequestIdMissing() {
        GatewaySecurityProperties properties = new GatewaySecurityProperties();
        properties.getReplay().setEnabled(true);
        properties.getReplay().setWindowSeconds(60);
        properties.getReplay().setProtectedPaths(List.of("/api/**"));

        ReplayProtectFilter filter = new ReplayProtectFilter(properties);
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/order/order/submit").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean invoked = new AtomicBoolean(false);
        GatewayFilterChain chain = e -> {
            invoked.set(true);
            return e.getResponse().setComplete();
        };

        filter.filter(exchange, chain).block();

        assertFalse(invoked.get());
        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldRejectDuplicateRequestIdWithinWindow() {
        GatewaySecurityProperties properties = new GatewaySecurityProperties();
        properties.getReplay().setEnabled(true);
        properties.getReplay().setWindowSeconds(60);
        properties.getReplay().setProtectedPaths(List.of("/api/**"));

        ReplayProtectFilter filter = new ReplayProtectFilter(properties);
        GatewayFilterChain chain = exchange -> exchange.getResponse().setComplete();

        MockServerHttpRequest firstRequest = MockServerHttpRequest.post("/api/order/order/submit")
                .header("X-Request-Id", "req-1")
                .build();
        MockServerWebExchange firstExchange = MockServerWebExchange.from(firstRequest);
        filter.filter(firstExchange, chain).block();

        MockServerHttpRequest secondRequest = MockServerHttpRequest.post("/api/order/order/submit")
                .header("X-Request-Id", "req-1")
                .build();
        MockServerWebExchange secondExchange = MockServerWebExchange.from(secondRequest);
        filter.filter(secondExchange, chain).block();

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, secondExchange.getResponse().getStatusCode());
    }

    @Test
    void shouldPassGetRequestWithoutReplayCheck() {
        GatewaySecurityProperties properties = new GatewaySecurityProperties();
        properties.getReplay().setEnabled(true);
        properties.getReplay().setProtectedPaths(List.of("/api/**"));

        ReplayProtectFilter filter = new ReplayProtectFilter(properties);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/product/brand/list").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean invoked = new AtomicBoolean(false);
        GatewayFilterChain chain = e -> {
            invoked.set(true);
            return e.getResponse().setComplete();
        };

        filter.filter(exchange, chain).block();

        assertTrue(invoked.get());
    }
}
