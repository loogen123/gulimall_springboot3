package com.lg.gulimail.member.domain.integration;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberIntegrationDomainServiceTest {
    private final MemberIntegrationDomainService memberIntegrationDomainService = new MemberIntegrationDomainService();

    @Test
    void validateQuoteCommandShouldRejectInvalidInput() {
        MemberIntegrationQuoteResult result = memberIntegrationDomainService.validateQuoteCommand(new MemberIntegrationCommand());
        assertFalse(result.isSuccess());
        assertEquals(10001, result.getCode());
    }

    @Test
    void resolveQuoteShouldCapByAvailableAndOrderTotal() {
        MemberIntegrationQuoteResult result =
                memberIntegrationDomainService.resolveQuote(1000, 900, new BigDecimal("5.00"));
        assertTrue(result.isSuccess());
        assertEquals(500, result.getUseIntegration());
        assertEquals(new BigDecimal("5.00"), result.getIntegrationAmount());
    }
}
