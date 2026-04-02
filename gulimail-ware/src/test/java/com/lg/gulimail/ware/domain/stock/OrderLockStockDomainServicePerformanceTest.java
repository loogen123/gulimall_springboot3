package com.lg.gulimail.ware.domain.stock;

import com.lg.common.vo.OrderItemVo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderLockStockDomainServicePerformanceTest {
    private final OrderLockStockDomainService domainService = new OrderLockStockDomainService();

    @Test
    void normalizeAndValidateShouldStayWithinThreshold() {
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            OrderLockStockCommand command = new OrderLockStockCommand();
            command.setOrderSn(" order-" + i + " ");
            command.setLocks(List.of(new OrderItemVo()));
            OrderLockStockCommand normalized = domainService.normalizeCommand(command);
            domainService.isValid(normalized);
        }
        long costMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue(costMs <= 1500);
    }
}
