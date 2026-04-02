package com.lg.gulimail.gateway.handler;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class GatewayExceptionHandlerTest {

    private final GatewayExceptionHandler handler = new GatewayExceptionHandler();

    @Test
    void handleResponseStatusException() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
        
        Mono<Void> result = handler.handle(exchange, ex);
        
        StepVerifier.create(result).verifyComplete();
        
        MockServerHttpResponse response = exchange.getResponse();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().isCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON));
        
        String body = response.getBodyAsString().block();
        assertTrue(body.contains("\"code\":404"));
        assertTrue(body.contains("\"msg\":\"Resource not found\""));
    }

    @Test
    void handleGenericException() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        RuntimeException ex = new RuntimeException("Unexpected error");
        
        Mono<Void> result = handler.handle(exchange, ex);
        
        StepVerifier.create(result).verifyComplete();
        
        MockServerHttpResponse response = exchange.getResponse();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        String body = response.getBodyAsString().block();
        assertTrue(body.contains("\"code\":500"));
    }
}
