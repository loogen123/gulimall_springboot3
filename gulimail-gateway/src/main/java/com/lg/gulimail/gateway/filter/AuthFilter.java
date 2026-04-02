package com.lg.gulimail.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.lg.common.exception.BizCodeEnum;
import com.lg.common.utils.R;
import com.lg.gulimail.gateway.config.GatewaySecurityProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {
    private final GatewaySecurityProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthFilter(GatewaySecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        GatewaySecurityProperties.Auth auth = properties.getAuth();
        if (!auth.isEnabled()) {
            return chain.filter(exchange);
        }
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }
        String path = exchange.getRequest().getPath().value();
        if (!matches(path, auth.getProtectedPaths()) || matches(path, auth.getIgnorePaths())) {
            return chain.filter(exchange);
        }
        String token = exchange.getRequest().getHeaders().getFirst(auth.getHeaderName());
        if (!StringUtils.hasText(token) || !token.equals(auth.getToken())) {
            return writeJson(exchange, HttpStatus.UNAUTHORIZED, BizCodeEnum.UNAUTHORIZED_EXCEPTION);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -200;
    }

    private boolean matches(String path, List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> writeJson(ServerWebExchange exchange, HttpStatus status, BizCodeEnum bizCodeEnum) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = JSON.toJSONString(R.error(bizCodeEnum.getCode(), bizCodeEnum.getMsg()))
                .getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
