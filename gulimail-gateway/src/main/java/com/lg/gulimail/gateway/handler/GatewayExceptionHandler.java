package com.lg.gulimail.gateway.handler;

import com.alibaba.fastjson.JSON;
import com.lg.common.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关全局异常处理，统一返回 JSON 格式
 */
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String msg = "系统异常，请稍后再试";

        if (ex instanceof ResponseStatusException) {
            status = (HttpStatus) ((ResponseStatusException) ex).getStatusCode();
            msg = ((ResponseStatusException) ex).getReason();
        }

        log.error("[网关异常处理] URI: {}, Status: {}, Error: {}", 
                exchange.getRequest().getURI(), status, ex.getMessage());

        response.setStatusCode(status);
        R r = R.error(status.value(), msg != null ? msg : status.getReasonPhrase());
        
        byte[] bytes = JSON.toJSONString(r).getBytes(StandardCharsets.UTF_8);
        DataBufferFactory bufferFactory = response.bufferFactory();
        DataBuffer buffer = bufferFactory.wrap(bytes);

        return response.writeWith(Mono.just(buffer));
    }
}
