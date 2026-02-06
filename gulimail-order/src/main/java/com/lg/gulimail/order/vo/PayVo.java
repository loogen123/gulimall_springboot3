package com.lg.gulimail.order.vo;

import lombok.Data;

@Data
public class PayVo {
    private String out_trade_no; // 订单号
    private String bill_model;   // 账单模型
    private String total_amount; // 订单总额
    private String subject;      // 订单标题
    private String body;         // 订单描述
}