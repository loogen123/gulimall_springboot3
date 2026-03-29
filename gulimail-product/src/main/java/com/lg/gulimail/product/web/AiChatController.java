package com.lg.gulimail.product.web;

import com.lg.gulimail.product.ai.MallAssistant;
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

@RestController
@RequestMapping("/api/product/ai")
@CrossOrigin
public class AiChatController {

    private static final Logger logger = LoggerFactory.getLogger(AiChatController.class);

    private static final String DEFAULT_ERROR_MESSAGE = "抱歉，AI助手暂时无法响应，请稍后再试。😊";
    private static final String STREAM_DONE_SIGNAL = "[DONE]";
    private static final long SSE_TIMEOUT = 300000L;

    @Autowired
    private MallAssistant mallAssistant;

    @GetMapping("/chat")
    public String chat(@RequestParam("msg") String msg) {
        logger.info("处理AI普通聊天请求，消息：{}", msg);
        
        try {
            String response = mallAssistant.chat(msg);
            logger.debug("AI响应生成完成，消息长度：{}", response.length());
            return response;
        } catch (Exception e) {
            logger.error("AI聊天处理异常，消息：{}", msg, e);
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    @GetMapping("/health")
    public String healthCheck() {
        logger.info("AI服务健康检查");
        
        try {
            String testResponse = mallAssistant.chat("你好");
            if (testResponse != null && !testResponse.trim().isEmpty()) {
                return "AI服务运行正常";
            }
            return "AI服务响应异常";
        } catch (Exception e) {
            logger.error("AI服务健康检查异常", e);
            return "AI服务异常：" + e.getMessage();
        }
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam("msg") String msg, HttpServletResponse response) {
        // 禁用Nginx缓存，解决流式卡顿问题
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Content-Type", "text/event-stream; charset=utf-8");
        
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

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
            
            // 0.31.0版本兼容的流式输出实现
            
            // 回归最纯粹的流式逻辑
            TokenStream tokenStream = mallAssistant.chatStream(msg);
            
            // 纯粹流式：tokenStream.onNext(token -> emitter.send(token)).start()
            tokenStream
                .onNext(token -> {
                    // 增强日志：打印Token，监控工具调用
                    logger.info("Token Received: " + token);
                    logger.debug("AI Token: {}", token);
                    
                    // 直接发送token，最纯粹的流式
                    if (token != null && !token.isEmpty()) {
                        try {
                            emitter.send(SseEmitter.event().data(token));
                        } catch (IOException e) {
                            logger.error("SSE发送token失败", e);
                        }
                    }
                })
                .onComplete(chatResponse -> {
                    try {
                        logger.debug("AI流式响应完成");
                        
                        // 发送完成信号
                        emitter.send(SseEmitter.event().data(STREAM_DONE_SIGNAL));
                        emitter.complete();
                    } catch (IOException e) {
                        logger.error("SSE发送完成信号失败", e);
                        emitter.completeWithError(e);
                    }
                })
                .onError(error -> {
                    // 统一异常处理
                    handleStreamError(emitter, new Exception(error), msg);
                })
                .start();  // 必须调用start()启动异步请求
            
        } catch (Exception e) {
            // 统一异常处理
            handleStreamError(emitter, e, msg);
        }

        return emitter;
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