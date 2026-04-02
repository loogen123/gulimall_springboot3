package com.lg.gulimail.order.domain.order;

import com.lg.gulimail.order.vo.OrderSubmitVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitCommand {
    private Long addrId;
    private Integer payType;
    private String orderToken;
    private BigDecimal payPrice;
    private Long couponId;
    private Integer useIntegration;
    private BigDecimal couponAmount;
    private BigDecimal integrationAmount;
    private String remarks;

    public static OrderSubmitCommand from(OrderSubmitVo vo) {
        OrderSubmitCommand command = new OrderSubmitCommand();
        if (vo == null) {
            return command;
        }
        command.setAddrId(vo.getAddrId());
        command.setPayType(vo.getPayType());
        command.setOrderToken(vo.getOrderToken());
        command.setPayPrice(vo.getPayPrice());
        command.setCouponId(vo.getCouponId());
        command.setUseIntegration(vo.getUseIntegration());
        command.setCouponAmount(vo.getCouponAmount());
        command.setIntegrationAmount(vo.getIntegrationAmount());
        command.setRemarks(vo.getRemarks());
        return command;
    }
}
