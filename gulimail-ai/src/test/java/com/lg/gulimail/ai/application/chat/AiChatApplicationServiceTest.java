package com.lg.gulimail.ai.application.chat;

import com.lg.gulimail.ai.application.port.out.AiChatPersistencePort;
import com.lg.gulimail.ai.domain.chat.AiChatDomainService;
import com.lg.gulimail.ai.model.entity.AiChatSessionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatApplicationServiceTest {
    @Mock
    private AiChatPersistencePort aiChatPersistencePort;

    private AiChatApplicationService aiChatApplicationService;

    @BeforeEach
    void setUp() {
        aiChatApplicationService = new AiChatApplicationService(aiChatPersistencePort, new AiChatDomainService());
    }

    @Test
    void startConversationShouldCreateSessionAndSaveMessageWhenSessionIdIsNull() {
        when(aiChatPersistencePort.createSession(10L, "新会话")).thenReturn(88L);
        aiChatApplicationService.startConversation(10L, null, "hello");
        verify(aiChatPersistencePort).createSession(10L, "新会话");
        verify(aiChatPersistencePort).saveMessage(88L, "user", "hello");
    }

    @Test
    void startConversationShouldReuseExistingSessionWhenOwnerMatched() {
        AiChatSessionEntity sessionEntity = new AiChatSessionEntity();
        sessionEntity.setId(66L);
        sessionEntity.setUserId(10L);
        when(aiChatPersistencePort.findSessionById(66L)).thenReturn(sessionEntity);
        aiChatApplicationService.startConversation(10L, 66L, "hello");
        verify(aiChatPersistencePort).findSessionById(66L);
        verify(aiChatPersistencePort).saveMessage(66L, "user", "hello");
        verify(aiChatPersistencePort, never()).createSession(10L, "新会话");
    }

    @Test
    void saveAssistantMessageShouldSkipBlankMessage() {
        aiChatApplicationService.saveAssistantMessage(66L, " ");
        verify(aiChatPersistencePort, never()).saveMessage(66L, "assistant", " ");
    }
}
