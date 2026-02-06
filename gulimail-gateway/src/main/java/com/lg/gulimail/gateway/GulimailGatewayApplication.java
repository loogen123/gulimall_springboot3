package com.lg.gulimail.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1.开启网关服务的注册发现
 *  1）配置nacos地址
 *  2）添加@EnableDiscoveryClient
 *
 *
 * 2.逻辑删除
 *  1）配置逻辑删除的组件bean（可省略）
 *  2）配置全局逻辑删除的规则（可省略）
 *  3)加上逻辑删除注解@TableLogic
 *
 */
@SpringBootApplication(
        scanBasePackages = {"com.lg.gulimail.gateway"}, // 只扫描网关自己的包
        exclude = {DataSourceAutoConfiguration.class}
)
@EnableDiscoveryClient
public class GulimailGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimailGatewayApplication.class, args);
    }

}
