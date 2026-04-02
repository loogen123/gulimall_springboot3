package com.lg.gulimail.member.domain.integration;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberIntegrationDomainServicePerformanceTest {
    @Test
    void shouldCompleteValidationAndQuoteWithinThreshold() {
        MemberIntegrationDomainService domainService = new MemberIntegrationDomainService();
        MemberIntegrationCommand command = new MemberIntegrationCommand();
        command.setMemberId(1L);
        command.setUseIntegration(100);
        command.setOrderTotal(new BigDecimal("9.90"));
        command.setOrderSn("order-sn");
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            domainService.validateQuoteCommand(command);
            domainService.validateMutationCommand(command);
            domainService.resolveQuote(1000, 100, new BigDecimal("9.90"));
        }
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis <= 1200, "member积分领域规则耗时过高: " + elapsedMillis + "ms");
    }
}
