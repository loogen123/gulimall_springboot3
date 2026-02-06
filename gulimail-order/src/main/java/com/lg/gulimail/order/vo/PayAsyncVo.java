package com.lg.gulimail.order.vo;

import lombok.Data;

@Data
public class PayAsyncVo {
    private String gmt_create;
    private String charset;
    private String gmt_payment;
    private String notify_time;
    private String subject;
    private String sign;
    private String buyer_id;
    private String body;
    private String invoice_amount;
    private String version;
    private String notify_id;
    private String fund_bill_list;
    private String notify_type;
    private String out_trade_no; // 订单号
    private String total_amount; // 支付总金额
    private String trade_status; // 交易状态（TRADE_SUCCESS）
    private String trade_no;     // 支付宝流水号
    private String auth_app_id;
    private String receipt_amount;
    private String point_amount;
    private String app_id;
    private String buyer_pay_amount;
    private String sign_type;
    private String seller_id;
}