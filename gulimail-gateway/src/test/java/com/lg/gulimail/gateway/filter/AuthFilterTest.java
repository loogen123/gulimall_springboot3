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

class AuthFilterTest {

    @Test
    void shouldRejectWhenAuthEnabledAndTokenInvalid() {
        GatewaySecurityProperties properties = new GatewaySecurityProperties();
        properties.getAuth().setEnabled(true);
        properties.getAuth().setToken("demo-token");
        properties.getAuth().setProtectedPaths(List.of("/api/**"));

        AuthFilter filter = new AuthFilter(properties);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/product/brand/list").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean invoked = new AtomicBoolean(false);
        GatewayFilterChain chain = e -> {
            invoked.set(true);
            return e.getResponse().setComplete();
        };

        filter.filter(exchange, chain).block();

        assertFalse(invoked.get());
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldPassWhenAuthDisabled() {
        GatewaySecurityProperties properties = new GatewaySecurityProperties();
        properties.getAuth().setEnabled(false);

        AuthFilter filter = new AuthFilter(properties);
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
