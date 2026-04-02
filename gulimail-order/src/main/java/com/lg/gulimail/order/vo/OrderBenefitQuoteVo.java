package com.lg.gulimail.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderBenefitQuoteVo {
    private Long couponId;
    private Integer useIntegration = 0;
    private BigDecimal couponAmount = BigDecimal.ZERO;
    private BigDecimal integrationAmount = BigDecimal.ZERO;
}
