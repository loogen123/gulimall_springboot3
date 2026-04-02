package com.lg.gulimail.ai.controller;

import com.lg.gulimail.ai.application.chat.AiChatApplicationService;
import com.lg.gulimail.ai.service.MallAssistant;
import dev.langchain4j.service.TokenStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import jakarta.servlet.http.HttpServletRequest;
import com.lg.gulimail.ai.config.FeignConfig;
import jakarta.servlet.http.HttpSession;
import com.lg.common.constant.AuthServerConstant;
import com.lg.common.exception.BizCodeEnum;
import com.lg.common.utils.RRException;
import com.lg.common.vo.MemberResponseVo;
import com.lg.gulimail.ai.model.entity.AiChatMessageEntity;
import com.lg.gulimail.ai.model.entity.AiChatSessionEntity;
import java.util.List;

@RestController
@RequestMapping("/api/ai/v1")
@CrossOrigin(origins = {"http://gulimail.com", "http://item.gulimail.com", "http://search.gulimail.com", "http://localhost:10000", "http://localhost:12000", "http://localhost:88", "http://localhost:3000", "http://localhost:5173", "http://localhost:8080", "http://localhost:8001"}, allowCredentials = "true")
public class AiChatController {

    private static final Logger logger = LoggerFactory.getLogger(AiChatController.class);

    private static final String STREAM_DONE_SIGNAL = "[DONE]";
    private static final long SSE_TIMEOUT = 300000L;
    private static final int MAX_MESSAGE_LENGTH = 2000;

    @Autowired
    private MallAssistant mallAssistant;

    @Autowired
    private AiChatApplicationService aiChatApplicationService;

    @GetMapping("/sessions")
    public List<AiChatSessionEntity> getSessions(HttpServletRequest request) {
        MemberResponseVo user = getLoginUser(request);
        if (user == null) {
            throw new RRException(BizCodeEnum.UNAUTHORIZED_EXCEPTION);
        }
        return aiChatApplicationService.getSessions(user.getId());
    }

    @GetMapping("/sessions/{id}/messages")
    public List<AiChatMessageEntity> getMessages(@PathVariable("id") Long sessionId, HttpServletRequest request) {
        if (sessionId == null || sessionId < 1) {
            throw new RRException("sessionId参数非法", BizCodeEnum.VAILD_EXCEPTION.getCode());
        }
        MemberResponseVo user = getLoginUser(request);
        if (user == null) {
            throw new RRException(BizCodeEnum.UNAUTHORIZED_EXCEPTION);
        }
        return aiChatApplicationService.getMessages(user.getId(), sessionId);
    }

    private MemberResponseVo getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        }
        return null;
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "AI Service v1 is UP";
    }

    @GetMapping("/chat/stream")
    public SseEmitter chatStream(@RequestParam("msg") String msg,
                                 @RequestParam(value = "sessionId", required = false) Long sessionId,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        if (msg == null || msg.isBlank()) {
            throw new RRException("消息不能为空", BizCodeEnum.VAILD_EXCEPTION.getCode());
        }
        if (msg.length() > MAX_MESSAGE_LENGTH) {
            throw new RRException("消息长度不能超过2000字符", BizCodeEnum.VAILD_EXCEPTION.getCode());
        }
        if (sessionId != null && sessionId < 1) {
            throw new RRException("sessionId参数非法", BizCodeEnum.VAILD_EXCEPTION.getCode());
        }
        MemberResponseVo user = getLoginUser(request);
        if (user == null) {
            throw new RRException(BizCodeEnum.UNAUTHORIZED_EXCEPTION);
        }
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            logger.debug("request has session id={}, creationTime={}, lastAccessTime={}",
                session.getId(), new Date(session.getCreationTime()), new Date(session.getLastAccessedTime()));
        } else {
            logger.debug("anonymous stateless request");
        }
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Content-Type", "text/event-stream; charset=utf-8");
        
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        String cookieToUse = request.getHeader("Cookie");
        logger.debug("cookie present in header: {}", cookieToUse != null && !cookieToUse.isEmpty());
        logger.debug("session user recognized userId={}", user.getId());

        if (cookieToUse != null && !cookieToUse.isEmpty()) {
            FeignConfig.USER_COOKIE_THREAD_LOCAL.set(cookieToUse);
        } else {
            logger.warn("no cookie available for downstream propagation");
        }

        emitter.onTimeout(() -> {
            logger.warn("SSE连接超时");
            FeignConfig.USER_COOKIE_THREAD_LOCAL.remove();
            emitter.complete();
        });

        emitter.onCompletion(() -> {
            logger.info("SSE连接完成");
            FeignConfig.USER_COOKIE_THREAD_LOCAL.remove();
        });

        try {
            logger.info("开始处理AI流式请求");
            emitter.send(SseEmitter.event().comment("connection established"));

            final Long finalSessionId = aiChatApplicationService.startConversation(user.getId(), sessionId, msg);

            StringBuilder fullAiResponse = new StringBuilder();
            TokenStream tokenStream;
            try {
                tokenStream = mallAssistant.chatStream(msg);
            } catch (Exception e) {
                logger.error("初始化AI对话流失败：{}", e.getMessage(), e);
                handleStreamError(emitter, e);
                FeignConfig.USER_COOKIE_THREAD_LOCAL.remove();
                return emitter;
            }
            
            tokenStream
                .onNext(token -> {
                    if (token != null && !token.isEmpty()) {
                        fullAiResponse.append(token);
                        try {
                            emitter.send(SseEmitter.event().data(token));
                        } catch (IOException e) {
                            logger.error("SSE发送token失败", e);
                        }
                    }
                })
                .onComplete(chatResponse -> {
                    try {
                        aiChatApplicationService.saveAssistantMessage(finalSessionId, fullAiResponse.toString());
                        emitter.send(SseEmitter.event().data(STREAM_DONE_SIGNAL));
                        emitter.complete();
                    } catch (IOException e) {
                        logger.error("SSE发送完成信号失败", e);
                        emitter.completeWithError(e);
                    } finally {
                        FeignConfig.USER_COOKIE_THREAD_LOCAL.remove();
                    }
                })
                .onError(error -> {
                    logger.error("LangChain4j 内部处理流时发生异常: ", error);
                    handleStreamError(emitter, new Exception(error));
                    FeignConfig.USER_COOKIE_THREAD_LOCAL.remove();
                })
                .start();
            
        } catch (Exception e) {
            handleStreamError(emitter, e);
            FeignConfig.USER_COOKIE_THREAD_LOCAL.remove();
        }

        return emitter;
    }

    private void handleStreamError(SseEmitter emitter, Exception e) {
        logger.error("AI流式处理异常", e);
        try {
            emitter.send(SseEmitter.event().data("抱歉，AI服务暂时不可用，请稍后重试。"));
            emitter.complete();
        } catch (IOException ex) {
            logger.error("SSE发送错误消息失败", ex);
            emitter.completeWithError(ex);
        }
    }

}
