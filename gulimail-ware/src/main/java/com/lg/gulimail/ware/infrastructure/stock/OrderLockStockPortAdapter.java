package com.lg.gulimail.ware.infrastructure.stock;

import com.lg.common.to.mq.WareSkuLockTo;
import com.lg.gulimail.ware.application.port.out.OrderLockStockPort;
import com.lg.gulimail.ware.service.WareSkuService;
import org.springframework.stereotype.Component;

@Component
public class OrderLockStockPortAdapter implements OrderLockStockPort {
    private final WareSkuService wareSkuService;

    public OrderLockStockPortAdapter(WareSkuService wareSkuService) {
        this.wareSkuService = wareSkuService;
    }

    @Override
    public void lockStock(WareSkuLockTo lockTo) {
        wareSkuService.orderLockStock(lockTo);
    }
}
