package com.lg.gulimail.member.domain.integration;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberIntegrationCommand {
    private Long memberId;
    private Integer useIntegration;
    private BigDecimal orderTotal;
    private String orderSn;
}
