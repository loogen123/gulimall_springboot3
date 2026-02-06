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
            // 设置 100ms 超时，快速判断代理软件是否运行
            socket.connect(new InetSocketAddress("127.0.0.1", 7897), 100);

            // 2. 如果端口通了，说明开启了梯子，给 RestTemplate 设置代理
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7897));
            factory.setProxy(proxy);
            System.out.println(">>> 检测到 Clash 已开启，RestTemplate 启用 7897 代理模式");
        } catch (Exception e) {
            // 3. 如果端口不通，走直连模式（虽然访问 GitHub 会失败，但不会导致项目启动报拒绝连接）
            System.out.println(">>> 未检测到 Clash 代理，RestTemplate 切换为直连模式");
        }

        // 4. 设置超时时间，防止 GitHub 响应过慢导致系统挂起
        factory.setConnectTimeout(10000); // 连接超时 10s
        factory.setReadTimeout(10000);    // 数据读取超时 10s

        return new RestTemplate(factory);
    }
}