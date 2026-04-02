package com.lg.gulimail.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CouponInfoVo {
    private Long id;
    private BigDecimal amount;
    private BigDecimal minPoint;
    private Date startTime;
    private Date endTime;
    private Integer publish;
}
