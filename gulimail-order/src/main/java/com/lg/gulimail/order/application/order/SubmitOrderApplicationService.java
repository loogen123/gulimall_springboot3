package com.lg.gulimail.order.application.order;

import com.lg.gulimail.order.application.port.out.OrderSubmitPort;
import com.lg.gulimail.order.domain.order.OrderSubmitCommand;
import com.lg.gulimail.order.domain.order.OrderSubmitDomainService;
import com.lg.gulimail.order.domain.order.OrderSubmitResult;
import com.lg.gulimail.order.vo.SubmitOrderResponseVo;
import org.springframework.stereotype.Service;

@Service
public class SubmitOrderApplicationService {
    private final OrderSubmitPort orderSubmitPort;
    private final OrderSubmitDomainService orderSubmitDomainService;

    public SubmitOrderApplicationService(OrderSubmitPort orderSubmitPort, OrderSubmitDomainService orderSubmitDomainService) {
        this.orderSubmitPort = orderSubmitPort;
        this.orderSubmitDomainService = orderSubmitDomainService;
    }

    public OrderSubmitResult submitOrder(OrderSubmitCommand command) {
        OrderSubmitCommand normalizedCommand = orderSubmitDomainService.normalizeCommand(command);
        SubmitOrderResponseVo responseVo = orderSubmitPort.submit(normalizedCommand);
        return orderSubmitDomainService.resolveResult(responseVo);
    }
}
