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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReplayProtectFilter implements GlobalFilter, Ordered {
    private final GatewaySecurityProperties properties;
    private final Map<String, Long> requestCache = new ConcurrentHashMap<>();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public ReplayProtectFilter(GatewaySecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        GatewaySecurityProperties.Replay replay = properties.getReplay();
        if (!replay.isEnabled()) {
            return chain.filter(exchange);
        }
        HttpMethod method = exchange.getRequest().getMethod();
        if (method == null || HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method) || HttpMethod.OPTIONS.equals(method)) {
            return chain.filter(exchange);
        }
        String path = exchange.getRequest().getPath().value();
        if (!matches(path, replay.getProtectedPaths())) {
            return chain.filter(exchange);
        }
        String requestId = exchange.getRequest().getHeaders().getFirst(replay.getRequestIdHeader());
        if (!StringUtils.hasText(requestId)) {
            return writeJson(exchange, HttpStatus.BAD_REQUEST, BizCodeEnum.VAILD_EXCEPTION);
        }

        long now = System.currentTimeMillis();
        long windowMs = Math.max(1, replay.getWindowSeconds()) * 1000L;
        cleanup(now, windowMs);
        Long firstSeen = requestCache.putIfAbsent(requestId, now);
        if (firstSeen != null && now - firstSeen <= windowMs) {
            return writeJson(exchange, HttpStatus.TOO_MANY_REQUESTS, BizCodeEnum.TOO_MANY_REQUESTS);
        }
        requestCache.put(requestId, now);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -190;
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

    private void cleanup(long now, long windowMs) {
        long expireMs = windowMs * 2;
        requestCache.entrySet().removeIf(entry -> now - entry.getValue() > expireMs);
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
