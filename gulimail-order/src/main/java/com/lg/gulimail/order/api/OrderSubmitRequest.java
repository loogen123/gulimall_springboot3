package com.lg.gulimail.order.api;

import com.lg.gulimail.order.domain.order.OrderSubmitCommand;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitRequest {
    private Long addrId;
    private Integer payType;
    private String orderToken;
    private BigDecimal payPrice;
    private Long couponId;
    private Integer useIntegration;
    private BigDecimal couponAmount;
    private BigDecimal integrationAmount;
    private String remarks;

    public OrderSubmitCommand toCommand() {
        OrderSubmitCommand command = new OrderSubmitCommand();
        command.setAddrId(addrId);
        command.setPayType(payType);
        command.setOrderToken(orderToken);
        command.setPayPrice(payPrice);
        command.setCouponId(couponId);
        command.setUseIntegration(useIntegration);
        command.setCouponAmount(couponAmount);
        command.setIntegrationAmount(integrationAmount);
        command.setRemarks(remarks);
        return command;
    }
}
