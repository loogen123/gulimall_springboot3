package com.lg.gulimail.order.domain.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderSubmitDomainServicePerformanceTest {
    private final OrderSubmitDomainService domainService = new OrderSubmitDomainService();

    @Test
    void normalizeAndResolveShouldStayWithinThreshold() {
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            OrderSubmitCommand command = new OrderSubmitCommand();
            command.setOrderToken(" token-" + i + " ");
            command.setRemarks(" remarks-" + i + " ");
            domainService.normalizeCommand(command);
            domainService.resolveResult(null);
        }
        long costMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue(costMs <= 1500);
    }
}
