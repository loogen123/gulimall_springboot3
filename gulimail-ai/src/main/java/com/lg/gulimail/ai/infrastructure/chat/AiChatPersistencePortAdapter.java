package com.lg.gulimail.ai.infrastructure.chat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lg.gulimail.ai.application.port.out.AiChatPersistencePort;
import com.lg.gulimail.ai.mapper.AiChatMessageMapper;
import com.lg.gulimail.ai.mapper.AiChatSessionMapper;
import com.lg.gulimail.ai.model.entity.AiChatMessageEntity;
import com.lg.gulimail.ai.model.entity.AiChatSessionEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiChatPersistencePortAdapter implements AiChatPersistencePort {
    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;

    public AiChatPersistencePortAdapter(AiChatSessionMapper sessionMapper, AiChatMessageMapper messageMapper) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    public List<AiChatSessionEntity> listSessionsByUserId(Long userId) {
        return sessionMapper.selectList(new QueryWrapper<AiChatSessionEntity>()
                .eq("user_id", userId)
                .orderByDesc("create_time"));
    }

    @Override
    public AiChatSessionEntity findSessionById(Long sessionId) {
        return sessionMapper.selectById(sessionId);
    }

    @Override
    public List<AiChatMessageEntity> listMessagesBySessionId(Long sessionId) {
        return messageMapper.selectList(new QueryWrapper<AiChatMessageEntity>()
                .eq("session_id", sessionId)
                .orderByAsc("create_time"));
    }

    @Override
    public Long createSession(Long userId, String title) {
        AiChatSessionEntity session = new AiChatSessionEntity();
        session.setUserId(userId);
        session.setTitle(title);
        sessionMapper.insert(session);
        return session.getId();
    }

    @Override
    public void saveMessage(Long sessionId, String role, String content) {
        AiChatMessageEntity message = new AiChatMessageEntity();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        messageMapper.insert(message);
    }
}
