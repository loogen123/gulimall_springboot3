package com.lg.gulimail.ai.application.chat;

import com.lg.common.utils.RRException;
import com.lg.gulimail.ai.application.port.out.AiChatPersistencePort;
import com.lg.gulimail.ai.domain.chat.AiChatDomainService;
import com.lg.gulimail.ai.model.entity.AiChatMessageEntity;
import com.lg.gulimail.ai.model.entity.AiChatSessionEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiChatApplicationIntegrationTest {
    @Test
    void conversationFlowShouldPersistUserAndAssistantMessages() {
        InMemoryAiChatPersistencePort persistencePort = new InMemoryAiChatPersistencePort();
        AiChatApplicationService applicationService =
                new AiChatApplicationService(persistencePort, new AiChatDomainService());

        Long sessionId = applicationService.startConversation(100L, null, "你好");
        applicationService.saveAssistantMessage(sessionId, "您好，我是AI助手");

        List<AiChatMessageEntity> messages = applicationService.getMessages(100L, sessionId);
        assertEquals(2, messages.size());
        assertEquals("user", messages.get(0).getRole());
        assertEquals("assistant", messages.get(1).getRole());
    }

    @Test
    void getMessagesShouldRejectWhenSessionNotOwned() {
        InMemoryAiChatPersistencePort persistencePort = new InMemoryAiChatPersistencePort();
        AiChatApplicationService applicationService =
                new AiChatApplicationService(persistencePort, new AiChatDomainService());
        Long sessionId = applicationService.startConversation(100L, null, "hello");

        RRException exception = assertThrows(RRException.class,
                () -> applicationService.getMessages(200L, sessionId));
        assertEquals(10003, exception.getCode());
    }

    private static class InMemoryAiChatPersistencePort implements AiChatPersistencePort {
        private final AtomicLong sessionIdGenerator = new AtomicLong(1);
        private final AtomicLong messageIdGenerator = new AtomicLong(1);
        private final List<AiChatSessionEntity> sessions = new ArrayList<>();
        private final List<AiChatMessageEntity> messages = new ArrayList<>();

        @Override
        public List<AiChatSessionEntity> listSessionsByUserId(Long userId) {
            return sessions.stream()
                    .filter(it -> userId.equals(it.getUserId()))
                    .sorted(Comparator.comparing(AiChatSessionEntity::getCreateTime).reversed())
                    .toList();
        }

        @Override
        public AiChatSessionEntity findSessionById(Long sessionId) {
            return sessions.stream().filter(it -> sessionId.equals(it.getId())).findFirst().orElse(null);
        }

        @Override
        public List<AiChatMessageEntity> listMessagesBySessionId(Long sessionId) {
            return messages.stream()
                    .filter(it -> sessionId.equals(it.getSessionId()))
                    .sorted(Comparator.comparing(AiChatMessageEntity::getCreateTime))
                    .toList();
        }

        @Override
        public Long createSession(Long userId, String title) {
            AiChatSessionEntity sessionEntity = new AiChatSessionEntity();
            sessionEntity.setId(sessionIdGenerator.getAndIncrement());
            sessionEntity.setUserId(userId);
            sessionEntity.setTitle(title);
            sessionEntity.setCreateTime(new Date());
            sessions.add(sessionEntity);
            return sessionEntity.getId();
        }

        @Override
        public void saveMessage(Long sessionId, String role, String content) {
            AiChatMessageEntity messageEntity = new AiChatMessageEntity();
            messageEntity.setId(messageIdGenerator.getAndIncrement());
            messageEntity.setSessionId(sessionId);
            messageEntity.setRole(role);
            messageEntity.setContent(content);
            messageEntity.setCreateTime(new Date());
            messages.add(messageEntity);
        }
    }
}
