package com.lg.gulimail.member.domain.integration;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberIntegrationQuoteResult {
    private Integer code;
    private String message;
    private Integer useIntegration;
    private BigDecimal integrationAmount;

    public static MemberIntegrationQuoteResult success(Integer useIntegration, BigDecimal integrationAmount) {
        MemberIntegrationQuoteResult result = new MemberIntegrationQuoteResult();
        result.setCode(0);
        result.setMessage("success");
        result.setUseIntegration(useIntegration);
        result.setIntegrationAmount(integrationAmount);
        return result;
    }

    public static MemberIntegrationQuoteResult failure(Integer code, String message) {
        MemberIntegrationQuoteResult result = new MemberIntegrationQuoteResult();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public boolean isSuccess() {
        return code != null && code == 0;
    }
}
