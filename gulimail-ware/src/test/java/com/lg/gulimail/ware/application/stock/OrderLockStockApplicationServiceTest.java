package com.lg.gulimail.ware.application.stock;

import com.lg.common.exception.NoStockException;
import com.lg.common.to.mq.WareSkuLockTo;
import com.lg.common.vo.OrderItemVo;
import com.lg.gulimail.ware.application.port.out.OrderLockStockPort;
import com.lg.gulimail.ware.domain.stock.OrderLockStockDomainService;
import com.lg.gulimail.ware.domain.stock.OrderLockStockResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderLockStockApplicationServiceTest {
    @Mock
    private OrderLockStockPort orderLockStockPort;
    @Mock
    private OrderLockStockDomainService domainService;
    @InjectMocks
    private OrderLockStockApplicationService applicationService;

    @Test
    void lockStockShouldReturnInvalidWhenCommandInvalid() {
        WareSkuLockTo lockTo = new WareSkuLockTo();
        when(domainService.normalizeCommand(any())).thenCallRealMethod();
        when(domainService.isValid(any())).thenReturn(false);

        OrderLockStockResult result = applicationService.lockStock(lockTo);

        assertEquals(10001, result.getCode());
        verifyNoInteractions(orderLockStockPort);
    }

    @Test
    void lockStockShouldReturnNoStockWhenExceptionThrown() {
        WareSkuLockTo lockTo = new WareSkuLockTo();
        lockTo.setOrderSn("order-1");
        lockTo.setLocks(List.of(new OrderItemVo()));
        when(domainService.normalizeCommand(any())).thenCallRealMethod();
        when(domainService.isValid(any())).thenReturn(true);
        doThrow(new NoStockException(1L)).when(orderLockStockPort).lockStock(any());

        OrderLockStockResult result = applicationService.lockStock(lockTo);

        assertEquals(21000, result.getCode());
    }

    @Test
    void lockStockShouldReturnSuccessWhenLocked() {
        WareSkuLockTo lockTo = new WareSkuLockTo();
        lockTo.setOrderSn("order-1");
        lockTo.setLocks(List.of(new OrderItemVo()));
        when(domainService.normalizeCommand(any())).thenCallRealMethod();
        when(domainService.isValid(any())).thenReturn(true);

        OrderLockStockResult result = applicationService.lockStock(lockTo);

        assertEquals(0, result.getCode());
        verify(orderLockStockPort).lockStock(any());
    }
}
