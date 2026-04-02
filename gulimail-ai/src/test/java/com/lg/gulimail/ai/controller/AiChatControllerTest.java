package com.lg.gulimail.ai.controller;

import com.lg.common.constant.AuthServerConstant;
import com.lg.common.utils.RRException;
import com.lg.common.vo.MemberResponseVo;
import com.lg.gulimail.ai.application.chat.AiChatApplicationService;
import com.lg.gulimail.ai.model.entity.AiChatSessionEntity;
import com.lg.gulimail.ai.service.MallAssistant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatControllerTest {
    private static final String LONG_MESSAGE = "a".repeat(2001);

    @Mock
    private MallAssistant mallAssistant;

    @Mock
    private AiChatApplicationService aiChatApplicationService;

    @InjectMocks
    private AiChatController aiChatController;

    @Test
    void chatStreamShouldRejectBlankMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        RRException exception = assertThrows(RRException.class,
                () -> aiChatController.chatStream(" ", null, request, response));

        assertEquals(10001, exception.getCode());
    }

    @Test
    void chatStreamShouldRejectWhenNotLoggedIn() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        RRException exception = assertThrows(RRException.class,
                () -> aiChatController.chatStream("hello", null, request, response));

        assertEquals(10002, exception.getCode());
    }

    @Test
    void chatStreamShouldRejectWhenMessageTooLong() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        RRException exception = assertThrows(RRException.class,
                () -> aiChatController.chatStream(LONG_MESSAGE, null, request, response));

        assertEquals(10001, exception.getCode());
    }

    @Test
    void getMessagesShouldRejectWhenSessionNotOwnedByCurrentUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MemberResponseVo member = new MemberResponseVo();
        member.setId(100L);
        request.getSession().setAttribute(AuthServerConstant.LOGIN_USER, member);
        AiChatSessionEntity chatSession = new AiChatSessionEntity();
        chatSession.setId(1L);
        chatSession.setUserId(200L);
        when(aiChatApplicationService.getMessages(100L, 1L)).thenThrow(new RRException("无权限", 10003));

        RRException exception = assertThrows(RRException.class, () -> aiChatController.getMessages(1L, request));

        assertEquals(10003, exception.getCode());
    }

    @Test
    void getMessagesShouldRejectWhenSessionIdInvalid() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        RRException exception = assertThrows(RRException.class, () -> aiChatController.getMessages(0L, request));

        assertEquals(10001, exception.getCode());
    }

    @Test
    void chatStreamShouldRejectWhenSessionIdInvalid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MemberResponseVo member = new MemberResponseVo();
        member.setId(100L);
        request.getSession().setAttribute(AuthServerConstant.LOGIN_USER, member);
        MockHttpServletResponse response = new MockHttpServletResponse();

        RRException exception = assertThrows(RRException.class,
                () -> aiChatController.chatStream("hello", 0L, request, response));

        assertEquals(10001, exception.getCode());
    }
}
