package com.lg.gulimail.product.domain.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkuItemDomainServiceTest {
    private final SkuItemDomainService skuItemDomainService = new SkuItemDomainService();

    @Test
    void validateShouldRejectWhenSkuIdInvalid() {
        SkuItemCommand command = skuItemDomainService.normalize(0L);
        SkuItemResult result = skuItemDomainService.validate(command);
        assertEquals(10001, result.getCode());
    }

    @Test
    void validateShouldPassWhenSkuIdValid() {
        SkuItemCommand command = skuItemDomainService.normalize(1L);
        SkuItemResult result = skuItemDomainService.validate(command);
        assertTrue(result.isSuccess());
        assertNull(result.getItem());
    }
}
