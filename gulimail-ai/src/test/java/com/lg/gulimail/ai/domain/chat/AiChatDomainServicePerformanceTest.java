package com.lg.gulimail.ai.domain.chat;

import com.lg.gulimail.ai.model.entity.AiChatSessionEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AiChatDomainServicePerformanceTest {
    @Test
    void shouldCompleteValidationWithinThreshold() {
        AiChatDomainService domainService = new AiChatDomainService();
        AiChatSessionEntity sessionEntity = new AiChatSessionEntity();
        sessionEntity.setUserId(100L);
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            domainService.validateMessage("hello " + i);
            domainService.validateSessionId(1L);
            domainService.assertSessionOwnedByUser(sessionEntity, 100L);
        }
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis <= 1200, "AI领域校验耗时过高: " + elapsedMillis + "ms");
    }
}
