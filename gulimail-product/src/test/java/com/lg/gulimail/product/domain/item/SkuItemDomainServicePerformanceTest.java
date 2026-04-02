package com.lg.gulimail.product.domain.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SkuItemDomainServicePerformanceTest {
    @Test
    void shouldCompleteValidationWithinThreshold() {
        SkuItemDomainService domainService = new SkuItemDomainService();
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            SkuItemCommand command = domainService.normalize(100L);
            domainService.validate(command);
        }
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis <= 1200, "SKU详情领域校验耗时过高: " + elapsedMillis + "ms");
    }
}
