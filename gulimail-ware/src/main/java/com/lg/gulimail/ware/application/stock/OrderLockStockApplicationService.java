package com.lg.gulimail.ware.application.stock;

import com.lg.common.exception.NoStockException;
import com.lg.common.to.mq.WareSkuLockTo;
import com.lg.gulimail.ware.application.port.out.OrderLockStockPort;
import com.lg.gulimail.ware.domain.stock.OrderLockStockCommand;
import com.lg.gulimail.ware.domain.stock.OrderLockStockDomainService;
import com.lg.gulimail.ware.domain.stock.OrderLockStockResult;
import org.springframework.stereotype.Service;

@Service
public class OrderLockStockApplicationService {
    private final OrderLockStockPort orderLockStockPort;
    private final OrderLockStockDomainService domainService;

    public OrderLockStockApplicationService(OrderLockStockPort orderLockStockPort, OrderLockStockDomainService domainService) {
        this.orderLockStockPort = orderLockStockPort;
        this.domainService = domainService;
    }

    public OrderLockStockResult lockStock(WareSkuLockTo lockTo) {
        OrderLockStockCommand command = domainService.normalizeCommand(OrderLockStockCommand.from(lockTo));
        if (!domainService.isValid(command)) {
            return OrderLockStockResult.invalidParam();
        }
        try {
            orderLockStockPort.lockStock(command.toLockTo());
            return OrderLockStockResult.success();
        } catch (NoStockException ex) {
            return OrderLockStockResult.noStock();
        }
    }
}
