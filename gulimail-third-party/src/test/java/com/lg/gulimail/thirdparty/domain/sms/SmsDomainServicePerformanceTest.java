package com.lg.gulimail.thirdparty.domain.sms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SmsDomainServicePerformanceTest {
    @Test
    void shouldCompleteValidationWithinThreshold() {
        SmsDomainService domainService = new SmsDomainService();
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            domainService.validateSend(domainService.normalizeSend("13800138000"));
            domainService.validateCheck(domainService.normalizeCheck("13800138000", "123456"));
        }
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis <= 1200, "短信领域校验耗时过高: " + elapsedMillis + "ms");
    }
}
