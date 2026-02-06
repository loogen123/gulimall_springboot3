package com.lg.common.to.mq;

import com.lg.common.vo.OrderItemVo;
import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockTo {
    private String orderSn; // 订单号
    private List<OrderItemVo> locks; // 需要锁定的商品信息
}