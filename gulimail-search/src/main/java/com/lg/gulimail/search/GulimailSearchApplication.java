package com.lg.gulimail.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GulimailSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimailSearchApplication.class, args);
    }

}
