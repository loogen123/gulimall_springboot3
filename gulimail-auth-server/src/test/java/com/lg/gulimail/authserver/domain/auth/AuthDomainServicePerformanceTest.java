package com.lg.gulimail.authserver.domain.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthDomainServicePerformanceTest {
    private final AuthDomainService authDomainService = new AuthDomainService();

    @Test
    void normalizeAndTokenExtractShouldStayWithinThreshold() {
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            authDomainService.normalizePhone(" 13800138000 ");
            authDomainService.extractAccessToken("{\"access_token\":\" token-" + i + "\"}");
        }
        long costMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue(costMs <= 1500);
    }
}
