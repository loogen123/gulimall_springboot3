package com.lg.gulimail.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com.lg.gulimail.coupon.dao")
@EnableDiscoveryClient
public class GulimailCouponApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimailCouponApplication.class, args);
    }

}
