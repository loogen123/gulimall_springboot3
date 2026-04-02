package com.lg.gulimail.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GuliMailCorsConfigurationTest {

    @Test
    void corsFilterShouldAllowConfiguredOrigin() {
        GuliMailCorsProperties properties = new GuliMailCorsProperties();
        properties.setAllowedOriginPatterns(List.of("http://*.gulimail.com"));
        properties.setAllowedMethods(List.of("GET", "OPTIONS"));
        properties.setAllowedHeaders(List.of("*"));
        properties.setExposedHeaders(List.of("X-Trace-Id"));
        properties.setAllowCredentials(true);
        properties.setMaxAgeSeconds(600);
        GuliMailCorsConfiguration configuration = new GuliMailCorsConfiguration(properties);

        MockServerHttpRequest request = MockServerHttpRequest.options("http://gateway.gulimail.com/api/test")
                .header(HttpHeaders.ORIGIN, "http://auth.gulimail.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name())
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = webExchange -> {
            throw new IllegalStateException("Preflight request should not continue the chain");
        };

        configuration.corsWebFilter().filter(exchange, chain).block();

        assertEquals("http://auth.gulimail.com",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertNotNull(exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
        assertEquals("600", exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
    }

    @Test
    void corsFilterShouldRejectOriginOutOfWhitelist() {
        GuliMailCorsProperties properties = new GuliMailCorsProperties();
        properties.setAllowedOriginPatterns(List.of("http://*.gulimail.com"));
        properties.setAllowedMethods(List.of("GET", "OPTIONS"));
        properties.setAllowedHeaders(List.of("*"));
        properties.setExposedHeaders(List.of("X-Trace-Id"));
        properties.setAllowCredentials(true);
        GuliMailCorsConfiguration configuration = new GuliMailCorsConfiguration(properties);

        MockServerHttpRequest request = MockServerHttpRequest.options("http://gateway.gulimail.com/api/test")
                .header(HttpHeaders.ORIGIN, "http://evil.example.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name())
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = webExchange -> {
            throw new IllegalStateException("Rejected preflight request should not continue the chain");
        };

        configuration.corsWebFilter().filter(exchange, chain).block();

        assertNull(exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void corsFilterShouldFallbackWhenListsNull() {
        GuliMailCorsProperties properties = new GuliMailCorsProperties();
        properties.setAllowedOriginPatterns(null);
        properties.setAllowedMethods(null);
        properties.setAllowedHeaders(null);
        properties.setExposedHeaders(null);
        properties.setMaxAgeSeconds(-1);
        GuliMailCorsConfiguration configuration = new GuliMailCorsConfiguration(properties);

        MockServerHttpRequest request = MockServerHttpRequest.options("http://gateway.gulimail.com/api/test")
                .header(HttpHeaders.ORIGIN, "http://auth.gulimail.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name())
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = webExchange -> {
            throw new IllegalStateException("Rejected preflight request should not continue the chain");
        };

        configuration.corsWebFilter().filter(exchange, chain).block();

        assertNull(exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertNull(exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
    }

    @Test
    void corsFilterShouldClampNegativeMaxAgeToZeroWhenOriginAllowed() {
        GuliMailCorsProperties properties = new GuliMailCorsProperties();
        properties.setAllowedOriginPatterns(List.of("http://*.gulimail.com"));
        properties.setAllowedMethods(List.of("GET", "OPTIONS"));
        properties.setAllowedHeaders(List.of("*"));
        properties.setAllowCredentials(true);
        properties.setMaxAgeSeconds(-1);
        GuliMailCorsConfiguration configuration = new GuliMailCorsConfiguration(properties);

        MockServerHttpRequest request = MockServerHttpRequest.options("http://gateway.gulimail.com/api/test")
                .header(HttpHeaders.ORIGIN, "http://auth.gulimail.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name())
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = webExchange -> {
            throw new IllegalStateException("Preflight request should not continue the chain");
        };

        configuration.corsWebFilter().filter(exchange, chain).block();

        assertEquals("0", exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
    }
}
