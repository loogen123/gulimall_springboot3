package com.lg.gulimail.order.infrastructure.order;

import com.lg.gulimail.order.application.port.out.OrderSubmitPort;
import com.lg.gulimail.order.domain.order.OrderSubmitCommand;
import com.lg.gulimail.order.service.OrderService;
import com.lg.gulimail.order.vo.OrderSubmitVo;
import com.lg.gulimail.order.vo.SubmitOrderResponseVo;
import org.springframework.stereotype.Component;

@Component
public class OrderSubmitPortAdapter implements OrderSubmitPort {
    private final OrderService orderService;

    public OrderSubmitPortAdapter(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public SubmitOrderResponseVo submit(OrderSubmitCommand command) {
        OrderSubmitVo vo = new OrderSubmitVo();
        vo.setAddrId(command.getAddrId());
        vo.setPayType(command.getPayType());
        vo.setOrderToken(command.getOrderToken());
        vo.setPayPrice(command.getPayPrice());
        vo.setCouponId(command.getCouponId());
        vo.setUseIntegration(command.getUseIntegration());
        vo.setCouponAmount(command.getCouponAmount());
        vo.setIntegrationAmount(command.getIntegrationAmount());
        vo.setRemarks(command.getRemarks());
        return orderService.submitOrder(vo);
    }
}
