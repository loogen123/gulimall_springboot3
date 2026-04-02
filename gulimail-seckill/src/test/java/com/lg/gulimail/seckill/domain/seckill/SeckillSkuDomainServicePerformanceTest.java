package com.lg.gulimail.seckill.domain.seckill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SeckillSkuDomainServicePerformanceTest {
    @Test
    void shouldCompleteValidationWithinThreshold() {
        SeckillSkuDomainService domainService = new SeckillSkuDomainService();
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            domainService.validate(domainService.normalize(1L));
            domainService.currentResult(null);
        }
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis <= 1200, "秒杀领域规则耗时过高: " + elapsedMillis + "ms");
    }
}
