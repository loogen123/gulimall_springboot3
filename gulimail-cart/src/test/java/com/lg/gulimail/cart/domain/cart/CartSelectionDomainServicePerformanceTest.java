package com.lg.gulimail.cart.domain.cart;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CartSelectionDomainServicePerformanceTest {
    private final CartSelectionDomainService domainService = new CartSelectionDomainService();

    @Test
    void normalizeAndValidateShouldStayWithinThreshold() {
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            CartSelectionCommand command = CartSelectionCommand.of((long) (i + 1), i % 2);
            domainService.normalize(command);
            domainService.validate(command);
        }
        long costMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue(costMs <= 1000);
    }
}
