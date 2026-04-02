package com.lg.gulimail.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraceIdFilterTest {
    @Test
    void shouldGenerateTraceIdWhenMissing() {
        TraceIdFilter filter = new TraceIdFilter();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/product/brand/list").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicReference<String> traceInRequest = new AtomicReference<>();
        GatewayFilterChain chain = e -> {
            traceInRequest.set(e.getRequest().getHeaders().getFirst("X-Trace-Id"));
            return e.getResponse().setComplete();
        };

        filter.filter(exchange, chain).block();

        assertNotNull(traceInRequest.get());
        assertTrue(traceInRequest.get().length() >= 16);
        assertEquals(traceInRequest.get(), exchange.getResponse().getHeaders().getFirst("X-Trace-Id"));
    }

    @Test
    void shouldUseExistingTraceId() {
        TraceIdFilter filter = new TraceIdFilter();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/product/brand/list")
                .header("X-Trace-Id", "trace-1")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicReference<String> traceInRequest = new AtomicReference<>();
        GatewayFilterChain chain = e -> {
            traceInRequest.set(e.getRequest().getHeaders().getFirst("X-Trace-Id"));
            return e.getResponse().setComplete();
        };

        filter.filter(exchange, chain).block();

        assertEquals("trace-1", traceInRequest.get());
        assertEquals("trace-1", exchange.getResponse().getHeaders().getFirst("X-Trace-Id"));
    }

    @Test
    void shouldFallbackToRequestIdWhenTraceIdMissing() {
        TraceIdFilter filter = new TraceIdFilter();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/product/brand/list")
                .header("X-Request-Id", "req-1")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicReference<String> traceInRequest = new AtomicReference<>();
        GatewayFilterChain chain = e -> {
            traceInRequest.set(e.getRequest().getHeaders().getFirst("X-Trace-Id"));
            return e.getResponse().setComplete();
        };

        filter.filter(exchange, chain).block();

        assertEquals("req-1", traceInRequest.get());
        assertEquals("req-1", exchange.getResponse().getHeaders().getFirst("X-Trace-Id"));
    }
}
