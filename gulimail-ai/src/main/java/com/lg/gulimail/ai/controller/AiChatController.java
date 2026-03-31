package com.lg.gulimail.ai.controller;

import com.lg.gulimail.ai.service.MallAssistant;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.service.TokenStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import jakarta.servlet.http.HttpServletRequest;
import com.lg.gulimail.ai.config.FeignConfig;
import jakarta.servlet.http.HttpSession;
import com.lg.common.constant.AuthServerConstant;
import com.lg.common.vo.MemberResponseVo;

import com.lg.gulimail.ai.mapper.AiChatMessageMapper;
import com.lg.gulimail.ai.mapper.AiChatSessionMapper;
import com.lg.gulimail.ai.model.entity.AiChatMessageEntity;
import com.lg.gulimail.ai.model.entity.AiChatSessionEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai/v1")
@CrossOrigin(origins = {"http://gulimail.com", "http://item.gulimail.com", "http://search.gulimail.com", "http://localhost:10000", "http://localhost:12000", "http://localhost:88", "http://localhost:3000", "http://localhost:5173", "http://localhost:8080", "http://localhost:8001"}, allowCredentials = "true")
public class AiChatController {

    private static final Logger logger = LoggerFactory.getLogger(AiChatController.class);

    private static final String DEFAULT_ERROR_MESSAGE = "抱歉，AI助手暂时无法响应，请稍后再试。😊";
    private static final String STREAM_DONE_SIGNAL = "[DONE]";
    private static final long SSE_TIMEOUT = 300000L;

    @Autowired
    private MallAssistant mallAssistant;

    @Autowired
    private AiChatSessionMapper sessionMapper;

    @Autowired
    private AiChatMessageMapper messageMapper;

    @GetMapping("/sessions")
    public List<AiChatSessionEntity> getSessions(HttpServletRequest request) {
        MemberResponseVo user = getLoginUser(request);
        if (user == null) return null;
        
        return sessionMapper.selectList(new QueryWrapper<AiChatSessionEntity>()
                .eq("user_id", user.getId())
                .orderByDesc("create_time"));
    }

    @GetMapping("/sessions/{id}/messages")
    public List<AiChatMessageEntity> getMessages(@PathVariable("id") Long sessionId, HttpServletRequest request) {
        MemberResponseVo user = getLoginUser(request);
        if (user == null) return null;

        // 简单越权校验
        AiChatSessionEntity session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(user.getId())) return null;

        return messageMapper.selectList(new QueryWrapper<AiChatMessageEntity>()
                .eq("session_id", sessionId)
                .orderByAsc("create_time"));
    }

    private MemberResponseVo getLoginUser(HttpServletRequest request) {
        // 关键修复：显式使用 request.getSession(false)，避免拦截器或逻辑层在未登录时频繁创建匿名 Session
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
        
        // 1. 记录请求进入时的 Session 状态
        HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("【Session诊断】请求进入 - 发现有效SessionID: {}, 创建时间: {}, 最后访问: {}", 
                session.getId(), new Date(session.getCreationTime()), new Date(session.getLastAccessedTime()));
        } else {
            logger.info("【Session诊断】请求进入 - 当前为无状态匿名请求");
        }
        // 禁用Nginx缓存，解决流式卡顿问题
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Content-Type", "text/event-stream; charset=utf-8");
        
        // 允许跨域及携带凭证
        String origin = request.getHeader("Origin");
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }
        
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        // 尝试从 Header 里拿
        String cookieToUse = request.getHeader("Cookie");
        logger.info("【会话传递】第一步：直接从 Request Header 获取 Cookie: {}", cookieToUse);
        logger.info("【会话传递】当前请求的所有 Header: ");
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            logger.info("    {} : {}", headerName, request.getHeader(headerName));
        }

        // 如果 Header 里没有，尝试从 request.getCookies() 中遍历获取 (Spring Session 通常能解析到)
        if (cookieToUse == null || cookieToUse.isEmpty()) {
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (jakarta.servlet.http.Cookie cookie : cookies) {
                    if ("GULISESSION".equals(cookie.getName())) {
                        cookieToUse = "GULISESSION=" + cookie.getValue();
                        logger.info("【会话传递】第二步：从 request.getCookies() 中成功提取到 GULISESSION: {}", cookieToUse);
                        break;
                    }
                }
            } else {
                logger.info("【会话传递】第二步：request.getCookies() 返回 null");
            }
        }
        
        // 验证 Spring Session 是否能获取到 (因为刚刚配置了 store-type: redis，此时Session应该是互通的)
        // 核心修改：统一通过 getLoginUser(request) 获取，确保不产生新的 Session
        MemberResponseVo user = getLoginUser(request);
        if (user != null) {
            logger.info("【会话传递】第三步：发现有效 Spring Session 用户：{}", user.getUsername());
            // 终极杀招：无论前面 Cookie 有没有拿到，只要 Spring Session 认出了用户，
            // 我们直接硬编码伪造一个合法的 GULISESSION 给 Feign 用（因为 Spring Session 默认就是用 Base64 编码的 session id 作为 Cookie 值）
            session = request.getSession(false);
            if (session != null) {
                String reconstructedCookie = "GULISESSION=" + new String(java.util.Base64.getEncoder().encode(session.getId().getBytes()));
                if (cookieToUse == null || cookieToUse.isEmpty()) {
                    logger.info("【会话传递】第三步：Header和Cookies都没拿到，但Session存在，使用重构的 Cookie: {}", reconstructedCookie);
                    cookieToUse = reconstructedCookie;
                }
            }
        } else {
            logger.warn("【会话传递】第三步：未发现有效登录 Session，当前为匿名请求。");
        }

        // 把最终拿到的 Cookie 存入全局兜底变量中
        if (cookieToUse != null && !cookieToUse.isEmpty()) {
            logger.info("【会话传递】最终决定使用 Cookie：{}", cookieToUse);
            FeignConfig.USER_COOKIE_THREAD_LOCAL.set(cookieToUse);
        } else {
            logger.error("【会话传递】彻底失败：前端没传 Cookie，Spring Session 也没解析到，当前请求是完全匿名的！");
        }

        emitter.onTimeout(() -> {
            logger.warn("SSE连接超时，消息：{}", msg);
            emitter.complete();
        });

        emitter.onCompletion(() -> {
            logger.info("SSE连接完成，消息：{}", msg);
        });

        try {
            logger.info("开始处理AI真正流式请求，消息：{}", msg);
            
            // 关键修改：先发送心跳，防止连接被Nginx立即回收
            emitter.send(SseEmitter.event().comment("connection established"));
            
            // 开启流式响应并持久化消息
            final Long finalSessionId = getOrCreateSession(sessionId, getLoginUser(request));
            saveMessage(finalSessionId, "user", msg);

            StringBuilder fullAiResponse = new StringBuilder();
            TokenStream tokenStream;
            try {
                tokenStream = mallAssistant.chatStream(msg);
            } catch (Exception e) {
                logger.error("初始化AI对话流失败：{}", e.getMessage(), e);
                handleStreamError(emitter, e, msg);
                return emitter;
            }
            
            tokenStream
                .onNext(token -> {
                    if (token != null && !token.isEmpty()) {
                        fullAiResponse.append(token);
                        try {
                            if (token.length() > 50) {
                                int chunkSize = 20;
                                for (int i = 0; i < token.length(); i += chunkSize) {
                                    int end = Math.min(token.length(), i + chunkSize);
                                    emitter.send(SseEmitter.event().data(token.substring(i, end)));
                                    Thread.sleep(2);
                                }
                            } else {
                                emitter.send(SseEmitter.event().data(token));
                            }
                        } catch (IOException | InterruptedException e) {
                            logger.error("SSE发送token失败", e);
                        }
                    }
                })
                .onComplete(chatResponse -> {
                    try {
                        saveMessage(finalSessionId, "assistant", fullAiResponse.toString());
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
                    handleStreamError(emitter, new Exception(error), msg);
                    FeignConfig.USER_COOKIE_THREAD_LOCAL.remove();
                })
                .start();
            
        } catch (Exception e) {
            handleStreamError(emitter, e, msg);
            FeignConfig.USER_COOKIE_THREAD_LOCAL.remove();
        }

        return emitter;
    }

    private Long getOrCreateSession(Long sessionId, MemberResponseVo user) {
        if (sessionId != null) return sessionId;
        
        AiChatSessionEntity session = new AiChatSessionEntity();
        if (user != null) session.setUserId(user.getId());
        session.setTitle("新会话");
        sessionMapper.insert(session);
        return session.getId();
    }

    private void saveMessage(Long sessionId, String role, String content) {
        AiChatMessageEntity message = new AiChatMessageEntity();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        messageMapper.insert(message);
    }

    private void handleStreamError(SseEmitter emitter, Exception e, String msg) {
        logger.error("AI流式处理异常，消息：{}", msg, e);
        try {
            emitter.send(SseEmitter.event().data("抱歉，AI服务暂时不可用，请稍后重试。"));
            emitter.complete();
        } catch (IOException ex) {
            logger.error("SSE发送错误消息失败", ex);
            emitter.completeWithError(ex);
        }
    }

}