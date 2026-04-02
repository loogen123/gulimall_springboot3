package com.lg.gulimail.ware.domain.stock;

import com.lg.common.vo.OrderItemVo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderLockStockDomainServiceTest {
    private final OrderLockStockDomainService domainService = new OrderLockStockDomainService();

    @Test
    void normalizeCommandShouldTrimOrderSn() {
        OrderLockStockCommand command = new OrderLockStockCommand();
        command.setOrderSn(" order-1 ");

        OrderLockStockCommand normalized = domainService.normalizeCommand(command);

        assertEquals("order-1", normalized.getOrderSn());
    }

    @Test
    void normalizeCommandShouldHandleNull() {
        OrderLockStockCommand normalized = domainService.normalizeCommand(null);

        assertNotNull(normalized);
    }

    @Test
    void isValidShouldReturnTrueWhenOrderSnAndLocksPresent() {
        OrderLockStockCommand command = new OrderLockStockCommand();
        command.setOrderSn("order-1");
        command.setLocks(List.of(new OrderItemVo()));

        assertTrue(domainService.isValid(command));
    }

    @Test
    void isValidShouldReturnFalseWhenLocksEmpty() {
        OrderLockStockCommand command = new OrderLockStockCommand();
        command.setOrderSn("order-1");
        command.setLocks(List.of());

        assertFalse(domainService.isValid(command));
    }
}
