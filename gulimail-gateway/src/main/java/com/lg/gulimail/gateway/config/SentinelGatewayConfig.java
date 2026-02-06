package com.lg.gulimail.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import jakarta.annotation.PostConstruct;

@Configuration
public class SentinelGatewayConfig {

    @PostConstruct
    public void init() {
        // 自定义限流后的返回逻辑
        GatewayCallbackManager.setBlockHandler((exchange, t) -> {
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue("{\"code\":429,\"msg\":\"系统繁忙，网关限流中\"}"));
        });
    }
}