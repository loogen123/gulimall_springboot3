package com.lg.gulimail.ai.domain.chat;

import com.lg.common.utils.RRException;
import com.lg.gulimail.ai.model.entity.AiChatSessionEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiChatDomainServiceTest {
    private final AiChatDomainService aiChatDomainService = new AiChatDomainService();

    @Test
    void validateMessageShouldRejectBlankMessage() {
        RRException exception = assertThrows(RRException.class, () -> aiChatDomainService.validateMessage(" "));
        assertEquals(10001, exception.getCode());
    }

    @Test
    void validateMessageShouldRejectMessageTooLong() {
        RRException exception = assertThrows(RRException.class,
                () -> aiChatDomainService.validateMessage("a".repeat(2001)));
        assertEquals(10001, exception.getCode());
    }

    @Test
    void validateSessionIdShouldRejectNonPositiveValue() {
        RRException exception = assertThrows(RRException.class, () -> aiChatDomainService.validateSessionId(0L));
        assertEquals(10001, exception.getCode());
    }

    @Test
    void assertSessionOwnedByUserShouldRejectWhenOwnerMismatch() {
        AiChatSessionEntity sessionEntity = new AiChatSessionEntity();
        sessionEntity.setUserId(2L);
        RRException exception = assertThrows(RRException.class,
                () -> aiChatDomainService.assertSessionOwnedByUser(sessionEntity, 1L));
        assertEquals(10003, exception.getCode());
    }

    @Test
    void assertSessionOwnedByUserShouldPassWhenOwnerMatched() {
        AiChatSessionEntity sessionEntity = new AiChatSessionEntity();
        sessionEntity.setUserId(1L);
        assertDoesNotThrow(() -> aiChatDomainService.assertSessionOwnedByUser(sessionEntity, 1L));
    }
}
