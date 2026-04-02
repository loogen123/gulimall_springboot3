package com.lg.gulimail.ware.controller;

import com.lg.common.vo.OrderItemVo;
import com.lg.common.to.mq.WareSkuLockTo;
import com.lg.common.utils.R;
import com.lg.common.vo.SkuHasStockVo;
import com.lg.gulimail.ware.application.stock.OrderLockStockApplicationService;
import com.lg.gulimail.ware.domain.stock.OrderLockStockResult;
import com.lg.gulimail.ware.entity.WareSkuEntity;
import com.lg.gulimail.ware.service.WareSkuService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WareSkuControllerTest {

    @Mock
    private WareSkuService wareSkuService;
    @Mock
    private OrderLockStockApplicationService orderLockStockApplicationService;

    @InjectMocks
    private WareSkuController wareSkuController;

    @Test
    void getSkusHasStockShouldReturnEmptyListWhenInputEmpty() {
        R result = wareSkuController.getSkusHasStock(Collections.emptyList());
        assertEquals(0, result.getCode());
        assertNotNull(result.get("data"));
    }

    @Test
    void getSkusHasStockShouldReturnServiceResult() {
        SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
        skuHasStockVo.setSkuId(1L);
        skuHasStockVo.setHasStock(true);
        when(wareSkuService.getSkusHasStock(List.of(1L))).thenReturn(List.of(skuHasStockVo));

        R result = wareSkuController.getSkusHasStock(List.of(1L));

        assertEquals(0, result.getCode());
        assertNotNull(result.get("data"));
        verify(wareSkuService).getSkusHasStock(List.of(1L));
    }

    @Test
    void orderLockStockShouldReturnOkWhenLockSuccess() {
        WareSkuLockTo lockTo = new WareSkuLockTo();
        lockTo.setOrderSn("order-1");
        lockTo.setLocks(List.of(new OrderItemVo()));
        when(orderLockStockApplicationService.lockStock(lockTo)).thenReturn(OrderLockStockResult.success());
        R result = wareSkuController.orderLockStock(lockTo);
        assertEquals(0, result.getCode());
    }

    @Test
    void orderLockStockShouldReturnBizCodeWhenNoStock() {
        WareSkuLockTo lockTo = new WareSkuLockTo();
        lockTo.setOrderSn("order-1");
        lockTo.setLocks(List.of(new OrderItemVo()));
        when(orderLockStockApplicationService.lockStock(lockTo)).thenReturn(OrderLockStockResult.noStock());
        R result = wareSkuController.orderLockStock(lockTo);
        assertEquals(21000, result.getCode());
    }

    @Test
    void orderLockStockShouldRejectWhenRequestInvalid() {
        WareSkuLockTo lockTo = new WareSkuLockTo();
        lockTo.setOrderSn(" ");
        when(orderLockStockApplicationService.lockStock(lockTo)).thenReturn(OrderLockStockResult.invalidParam());

        R result = wareSkuController.orderLockStock(lockTo);

        assertEquals(10001, result.getCode());
    }

    @Test
    void saveShouldRejectWhenBodyNull() {
        R result = wareSkuController.save(null);

        assertEquals(10001, result.getCode());
        verifyNoInteractions(wareSkuService);
    }

    @Test
    void updateShouldRejectWhenIdNull() {
        WareSkuEntity wareSkuEntity = new WareSkuEntity();

        R result = wareSkuController.update(wareSkuEntity);

        assertEquals(10001, result.getCode());
        verifyNoInteractions(wareSkuService);
    }

    @Test
    void deleteShouldRejectWhenIdsEmpty() {
        R result = wareSkuController.delete(new Long[0]);

        assertEquals(10001, result.getCode());
        verifyNoInteractions(wareSkuService);
    }
}
