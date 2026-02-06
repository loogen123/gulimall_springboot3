package com.lg.gulimail.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

//启动sentinel
// java "-Dserver.port=8333" "-Dcsp.sentinel.dashboard.server=localhost:8333" "-Dproject.name=sentinel-dashboard" -jar sentinel-dashboard-1.8.6.jar
@EnableFeignClients
@EnableAsync      // 开启异步任务支持
@EnableScheduling // 开启定时任务支持
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GulimailSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimailSeckillApplication.class, args);
    }

}
