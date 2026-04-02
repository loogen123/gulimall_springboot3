package com.lg.gulimail.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TraceIdFilterPerformanceTest {
    @Test
    void shouldCompleteTraceInjectionWithinThreshold() {
        TraceIdFilter filter = new TraceIdFilter();
        GatewayFilterChain chain = exchange -> exchange.getResponse().setComplete();
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/product/brand/list").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            filter.filter(exchange, chain).block();
        }
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis <= 3000, "TraceId 注入耗时过高: " + elapsedMillis + "ms");
    }
}
