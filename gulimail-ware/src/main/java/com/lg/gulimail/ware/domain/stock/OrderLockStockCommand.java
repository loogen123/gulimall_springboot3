package com.lg.gulimail.ware.domain.stock;

import com.lg.common.to.mq.WareSkuLockTo;
import com.lg.common.vo.OrderItemVo;
import lombok.Data;

import java.util.List;

@Data
public class OrderLockStockCommand {
    private String orderSn;
    private List<OrderItemVo> locks;

    public static OrderLockStockCommand from(WareSkuLockTo lockTo) {
        OrderLockStockCommand command = new OrderLockStockCommand();
        if (lockTo == null) {
            return command;
        }
        command.setOrderSn(lockTo.getOrderSn());
        command.setLocks(lockTo.getLocks());
        return command;
    }

    public WareSkuLockTo toLockTo() {
        WareSkuLockTo lockTo = new WareSkuLockTo();
        lockTo.setOrderSn(orderSn);
        lockTo.setLocks(locks);
        return lockTo;
    }
}
