package com.lg.gulimail.ware.domain.stock;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OrderLockStockDomainService {
    public OrderLockStockCommand normalizeCommand(OrderLockStockCommand command) {
        if (command == null) {
            return new OrderLockStockCommand();
        }
        if (StringUtils.hasText(command.getOrderSn())) {
            command.setOrderSn(command.getOrderSn().trim());
        }
        return command;
    }

    public boolean isValid(OrderLockStockCommand command) {
        return command != null
                && StringUtils.hasText(command.getOrderSn())
                && command.getLocks() != null
                && !command.getLocks().isEmpty();
    }
}
