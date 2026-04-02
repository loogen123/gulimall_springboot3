package com.lg.gulimail.ware.application.port.out;

import com.lg.common.to.mq.WareSkuLockTo;

public interface OrderLockStockPort {
    void lockStock(WareSkuLockTo lockTo);
}
