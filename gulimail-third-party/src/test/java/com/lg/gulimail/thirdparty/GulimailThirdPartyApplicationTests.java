package com.lg.gulimail.thirdparty;

import com.lg.gulimail.thirdparty.service.AliyunSmsService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class GulimailThirdPartyApplicationTests {
    @MockBean
    private AliyunSmsService aliyunSmsService;

    @Test
    void contextLoads() {
    }
}
