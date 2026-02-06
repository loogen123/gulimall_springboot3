package com.lg.gulimail.ware;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@MapperScan("com.lg.gulimail.ware.dao")
@EnableDiscoveryClient
@SpringBootTest
class GulimailWareApplicationTests {

    @Test
    void contextLoads() {
    }

}
