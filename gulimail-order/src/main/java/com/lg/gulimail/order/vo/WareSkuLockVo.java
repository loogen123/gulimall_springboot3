package com.lg.gulimail.order.vo;

import com.lg.common.vo.OrderItemVo;
import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {
    private String orderSn; // 订单号
    private List<OrderItemVo> locks; // 所有的订单项
}