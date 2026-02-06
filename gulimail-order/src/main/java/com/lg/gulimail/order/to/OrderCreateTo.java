package com.lg.gulimail.order.to;

import com.lg.gulimail.order.entity.OrderEntity;
import com.lg.gulimail.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {
    private OrderEntity order; // 订单主体
    private List<OrderItemEntity> orderItems; // 订单详情项
    private BigDecimal payPrice; // 后端计算的应付价格
    private BigDecimal fare;     // 运费
}