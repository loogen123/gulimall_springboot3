package com.lg.gulimail.cart.domain.cart;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartSelectionDomainServiceTest {
    private final CartSelectionDomainService domainService = new CartSelectionDomainService();

    @Test
    void validateShouldRejectInvalidSkuId() {
        CartSelectionResult result = domainService.validate(CartSelectionCommand.of(0L, 1));

        assertEquals(10001, result.getCode());
        assertEquals("skuId参数非法", result.getMessage());
    }

    @Test
    void validateShouldRejectInvalidCheck() {
        CartSelectionResult result = domainService.validate(CartSelectionCommand.of(1L, 2));

        assertEquals(10001, result.getCode());
        assertEquals("check参数非法", result.getMessage());
    }

    @Test
    void validateShouldPassForValidCommand() {
        CartSelectionResult result = domainService.validate(CartSelectionCommand.of(1L, 1));

        assertTrue(result.isSuccess());
    }
}
