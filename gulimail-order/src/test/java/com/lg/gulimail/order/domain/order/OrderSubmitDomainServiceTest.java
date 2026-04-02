package com.lg.gulimail.order.domain.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderSubmitDomainServiceTest {
    private final OrderSubmitDomainService domainService = new OrderSubmitDomainService();

    @Test
    void normalizeCommandShouldTrimTokenAndRemarks() {
        OrderSubmitCommand command = new OrderSubmitCommand();
        command.setOrderToken(" token-1 ");
        command.setRemarks(" hello ");

        OrderSubmitCommand normalized = domainService.normalizeCommand(command);

        assertEquals("token-1", normalized.getOrderToken());
        assertEquals("hello", normalized.getRemarks());
    }

    @Test
    void normalizeCommandShouldHandleNullInput() {
        OrderSubmitCommand normalized = domainService.normalizeCommand(null);

        assertNotNull(normalized);
    }

    @Test
    void resolveResultShouldFallbackToTokenExpiredWhenResponseNull() {
        OrderSubmitResult result = domainService.resolveResult(null);

        assertEquals(OrderSubmitResult.TOKEN_EXPIRED, result.normalizeCode());
    }
}
