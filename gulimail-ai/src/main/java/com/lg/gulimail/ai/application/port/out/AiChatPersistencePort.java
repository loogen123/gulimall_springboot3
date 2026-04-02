package com.lg.gulimail.ai.application.port.out;

import com.lg.gulimail.ai.model.entity.AiChatMessageEntity;
import com.lg.gulimail.ai.model.entity.AiChatSessionEntity;

import java.util.List;

public interface AiChatPersistencePort {
    List<AiChatSessionEntity> listSessionsByUserId(Long userId);

    AiChatSessionEntity findSessionById(Long sessionId);

    List<AiChatMessageEntity> listMessagesBySessionId(Long sessionId);

    Long createSession(Long userId, String title);

    void saveMessage(Long sessionId, String role, String content);
}
