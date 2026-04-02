package com.lg.gulimail.ai.domain.chat;

import com.lg.common.exception.BizCodeEnum;
import com.lg.common.utils.RRException;
import com.lg.gulimail.ai.model.entity.AiChatSessionEntity;
import org.springframework.stereotype.Service;

@Service
public class AiChatDomainService {
    private static final int MAX_MESSAGE_LENGTH = 2000;

    public void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new RRException("消息不能为空", BizCodeEnum.VAILD_EXCEPTION.getCode());
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new RRException("消息长度不能超过2000字符", BizCodeEnum.VAILD_EXCEPTION.getCode());
        }
    }

    public void validateSessionId(Long sessionId) {
        if (sessionId != null && sessionId < 1) {
            throw new RRException("sessionId参数非法", BizCodeEnum.VAILD_EXCEPTION.getCode());
        }
    }

    public void assertSessionOwnedByUser(AiChatSessionEntity session, Long userId) {
        if (session == null || userId == null || !userId.equals(session.getUserId())) {
            throw new RRException(BizCodeEnum.FORBIDDEN_EXCEPTION);
        }
    }
}
