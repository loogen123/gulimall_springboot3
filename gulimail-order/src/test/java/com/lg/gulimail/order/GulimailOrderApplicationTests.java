package com.lg.gulimail.order;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@MapperScan("com.lg.gulimail.order.dao")
@EnableDiscoveryClient
@SpringBootTest
class GulimailOrderApplicationTests {

    @Test
    void contextLoads() {
    }

}
