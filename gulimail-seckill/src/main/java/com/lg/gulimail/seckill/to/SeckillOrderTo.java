package com.lg.gulimail.seckill.to;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SeckillOrderTo {
    private String orderSn; // 订单号
    private Long promotionSessionId; // 场次ID
    private Long skuId; // 商品ID
    private BigDecimal seckillPrice; // 秒杀价格
    private Integer num; // 购买数量
    private Long memberId; // 会员ID
}