package com.lg.gulimail.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.lg.common.exception.BizCodeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import jakarta.annotation.PostConstruct;

@Configuration
public class SentinelGatewayConfig {
    private static final int DEFAULT_HTTP_BLOCK_CODE = 429;

    @Value("${gulimail.gateway.sentinel.block.code:429}")
    private int blockCode;

    @Value("${gulimail.gateway.sentinel.block.msg:系统繁忙，网关限流中}")
    private String blockMsg;

    @PostConstruct
    public void init() {
        int responseCode = resolveResponseCode(blockCode);
        String responseMsg = (blockMsg == null || blockMsg.isEmpty() || "系统繁忙，网关限流中".equals(blockMsg)) 
                ? BizCodeEnum.TOO_MANY_REQUESTS.getMsg() : blockMsg;
        
        HttpStatus status = HttpStatus.resolve(responseCode);
        String responseBody = buildResponseBody(BizCodeEnum.TOO_MANY_REQUESTS.getCode(), responseMsg);
        HttpStatus finalStatus = status;
        String finalResponseBody = responseBody;
        GatewayCallbackManager.setBlockHandler((exchange, t) -> {
            return ServerResponse.status(finalStatus)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(finalResponseBody));
        });
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private int resolveResponseCode(int configuredCode) {
        HttpStatus status = HttpStatus.resolve(configuredCode);
        if (status == null) {
            return DEFAULT_HTTP_BLOCK_CODE;
        }
        return configuredCode;
    }

    private String buildResponseBody(int code, String msg) {
        return "{\"code\":" + code + ",\"msg\":\"" + escapeJson(msg) + "\"}";
    }
}
