package com.lg.gulimail.member.domain.integration;

import com.lg.common.exception.BizCodeEnum;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class MemberIntegrationDomainService {
    public MemberIntegrationCommand normalize(MemberIntegrationCommand command) {
        if (command == null) {
            return null;
        }
        if (command.getOrderSn() != null) {
            command.setOrderSn(command.getOrderSn().trim());
        }
        return command;
    }

    public MemberIntegrationQuoteResult validateQuoteCommand(MemberIntegrationCommand command) {
        if (command == null || command.getMemberId() == null || command.getMemberId() <= 0
                || command.getUseIntegration() == null || command.getUseIntegration() < 0
                || command.getOrderTotal() == null || command.getOrderTotal().compareTo(BigDecimal.ZERO) < 0) {
            return MemberIntegrationQuoteResult.failure(BizCodeEnum.VAILD_EXCEPTION.getCode(), "请求参数不合法");
        }
        return MemberIntegrationQuoteResult.success(0, BigDecimal.ZERO);
    }

    public MemberIntegrationMutationResult validateMutationCommand(MemberIntegrationCommand command) {
        if (command == null || command.getMemberId() == null || command.getMemberId() <= 0
                || command.getUseIntegration() == null || command.getUseIntegration() <= 0
                || command.getOrderSn() == null || command.getOrderSn().isBlank()) {
            return MemberIntegrationMutationResult.failure(BizCodeEnum.VAILD_EXCEPTION.getCode(), "请求参数不合法");
        }
        return MemberIntegrationMutationResult.success();
    }

    public MemberIntegrationQuoteResult resolveQuote(Integer availableIntegration, Integer useIntegration, BigDecimal orderTotal) {
        int safeAvailable = availableIntegration == null ? 0 : Math.max(availableIntegration, 0);
        int safeUse = useIntegration == null ? 0 : Math.max(useIntegration, 0);
        int realUse = Math.min(safeUse, safeAvailable);
        BigDecimal integrationAmount = new BigDecimal(realUse).divide(new BigDecimal("100"), 2, RoundingMode.DOWN);
        if (orderTotal != null && integrationAmount.compareTo(orderTotal) > 0) {
            integrationAmount = orderTotal;
            realUse = orderTotal.multiply(new BigDecimal("100")).intValue();
        }
        return MemberIntegrationQuoteResult.success(realUse, integrationAmount);
    }

    public String deductNote(String orderSn) {
        return "ORDER_DEDUCT:" + orderSn;
    }

    public String revertNote(String orderSn) {
        return "ORDER_REVERT:" + orderSn;
    }
}
