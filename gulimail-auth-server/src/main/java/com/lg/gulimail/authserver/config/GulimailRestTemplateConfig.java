package com.lg.gulimail.authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

@Configuration
public class GulimailRestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 1. 探测本地 Clash 端口 7897 是否开启
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 7897), 100);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7897));
            factory.setProxy(proxy);
        } catch (Exception e) {
        }

        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);

        return new RestTemplate(factory);
    }
}
