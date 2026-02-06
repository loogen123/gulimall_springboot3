package com.lg.gulimail.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {
    /** 收货地址的id */
    private Long addrId;

    /** 支付方式 (比如：1-在线支付，2-货到付款) */
    private Integer payType = 1;

    /** 防重令牌 */
    private String orderToken;

    /** 应付总额（用于后端验价：对比用户看到的价格和后台实时算的价格） */
    private BigDecimal payPrice;

    /** 订单备注 */
    private String remarks;

    // 注意：不需要提交购物车里的商品 ID。
    // 为了安全，后端应该重新去 Redis 抓取当前用户勾选的商品。
}