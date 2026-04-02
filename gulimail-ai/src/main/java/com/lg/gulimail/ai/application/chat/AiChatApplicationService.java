package com.lg.gulimail.ai.application.chat;

import com.lg.gulimail.ai.application.port.out.AiChatPersistencePort;
import com.lg.gulimail.ai.domain.chat.AiChatDomainService;
import com.lg.gulimail.ai.model.entity.AiChatMessageEntity;
import com.lg.gulimail.ai.model.entity.AiChatSessionEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiChatApplicationService {
    private static final String DEFAULT_SESSION_TITLE = "新会话";
    private static final String USER_ROLE = "user";
    private static final String ASSISTANT_ROLE = "assistant";

    private final AiChatPersistencePort aiChatPersistencePort;
    private final AiChatDomainService aiChatDomainService;

    public AiChatApplicationService(AiChatPersistencePort aiChatPersistencePort,
                                    AiChatDomainService aiChatDomainService) {
        this.aiChatPersistencePort = aiChatPersistencePort;
        this.aiChatDomainService = aiChatDomainService;
    }

    public List<AiChatSessionEntity> getSessions(Long userId) {
        return aiChatPersistencePort.listSessionsByUserId(userId);
    }

    public List<AiChatMessageEntity> getMessages(Long userId, Long sessionId) {
        aiChatDomainService.validateSessionId(sessionId);
        AiChatSessionEntity session = aiChatPersistencePort.findSessionById(sessionId);
        aiChatDomainService.assertSessionOwnedByUser(session, userId);
        return aiChatPersistencePort.listMessagesBySessionId(sessionId);
    }

    public Long startConversation(Long userId, Long sessionId, String userMessage) {
        aiChatDomainService.validateMessage(userMessage);
        aiChatDomainService.validateSessionId(sessionId);
        Long finalSessionId = getOrCreateSession(userId, sessionId);
        aiChatPersistencePort.saveMessage(finalSessionId, USER_ROLE, userMessage);
        return finalSessionId;
    }

    public void saveAssistantMessage(Long sessionId, String assistantMessage) {
        if (assistantMessage == null || assistantMessage.isBlank()) {
            return;
        }
        aiChatPersistencePort.saveMessage(sessionId, ASSISTANT_ROLE, assistantMessage);
    }

    private Long getOrCreateSession(Long userId, Long sessionId) {
        if (sessionId != null) {
            AiChatSessionEntity session = aiChatPersistencePort.findSessionById(sessionId);
            aiChatDomainService.assertSessionOwnedByUser(session, userId);
            return sessionId;
        }
        return aiChatPersistencePort.createSession(userId, DEFAULT_SESSION_TITLE);
    }
}
