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
import jakarta.servlet.http.HttpServletRequest;
import com.lg.gulimail.ai.config.FeignConfig;
import jakarta.servlet.http.HttpSession;
import com.lg.common.constant.AuthServerConstant;
import com.lg.common.vo.MemberResponseVo;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://gulimail.com", "http://item.gulimail.com", "http://search.gulimail.com", "http://localhost:10000", "http://localhost:12000", "http://localhost:88"}, allowCredentials = "true")
public class AiChatController {

    private static final Logger logger = LoggerFactory.getLogger(AiChatController.class);

    private static final String DEFAULT_ERROR_MESSAGE = "抱歉，AI助手暂时无法响应，请稍后再试。😊";
    private static final String STREAM_DONE_SIGNAL = "[DONE]";
    private static final long SSE_TIMEOUT = 300000L;

    // 终极兜底方案：存放最近一次请求的 Cookie。
    // 在单用户/本地测试场景下，这能绝对保证异步线程拿到 Cookie
    public static volatile String GLOBAL_LAST_COOKIE = null;

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
    public SseEmitter chatStream(@RequestParam("msg") String msg, 
                                 HttpServletRequest request, 
                                 HttpServletResponse response) {
        // 禁用Nginx缓存，解决流式卡顿问题
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Content-Type", "text/event-stream; charset=utf-8");
        
        // 允许跨域及携带凭证
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        
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
        HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("【会话传递】第三步：发现 Spring Session, ID: {}", session.getId());
            MemberResponseVo user = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
            if (user != null) {
                logger.info("【会话传递】第三步：成功从 Spring Session 中获取到登录用户：{}", user.getUsername());
                // 终极杀招：无论前面 Cookie 有没有拿到，只要 Spring Session 认出了用户，
                // 我们直接硬编码伪造一个合法的 GULISESSION 给 Feign 用（因为 Spring Session 默认就是用 Base64 编码的 session id 作为 Cookie 值）
                String reconstructedCookie = "GULISESSION=" + new String(java.util.Base64.getEncoder().encode(session.getId().getBytes()));
                if (cookieToUse == null || cookieToUse.isEmpty()) {
                    logger.info("【会话传递】第三步：Header和Cookies都没拿到，但Session存在，使用重构的 Cookie: {}", reconstructedCookie);
                    cookieToUse = reconstructedCookie;
                }
            } else {
                logger.warn("【会话传递】第三步：Session 存在，但里面没有 LOGIN_USER 属性！这说明这是个匿名Session。");
            }
        } else {
            logger.warn("【会话传递】第三步：request.getSession(false) 返回 null！这说明 Spring Session 根本没有生效，或者前端连 SessionID 都没传过来！");
        }

        // 把最终拿到的 Cookie 存入全局兜底变量中
        if (cookieToUse != null && !cookieToUse.isEmpty()) {
            logger.info("【会话传递】最终决定使用 Cookie：{}", cookieToUse);
            FeignConfig.USER_COOKIE_THREAD_LOCAL.set(cookieToUse);
            GLOBAL_LAST_COOKIE = cookieToUse;
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
            
            // 0.31.0版本兼容的流式输出实现
            
            // 捕获LangChain4j可能的初始化异常
            TokenStream tokenStream;
            try {
                tokenStream = mallAssistant.chatStream(msg);
            } catch (Exception e) {
                logger.error("初始化AI对话流失败：{}", e.getMessage(), e);
                handleStreamError(emitter, e, msg);
                return emitter;
            }
            
            // 纯粹流式与分块结合：如果是工具调用返回的超大 Token，后端适当分块发送，避免前端缓冲区瞬间被打爆，
            // 但不使用长 Thread.sleep 阻塞。前端再结合 JS 队列实现极致平滑。
            tokenStream
                .onNext(token -> {
                    // 增强日志：打印Token，监控工具调用
                    logger.info("Token Received (Length: {}): {}", token != null ? token.length() : 0, token);
                    
                    if (token != null && !token.isEmpty()) {
                        try {
                            if (token.length() > 50) {
                                // 对于特别巨大的 Token (例如查完订单返回的几百字)，切成小块发送，避免单次网络包过大
                                int chunkSize = 20; // 每次发20个字符
                                for (int i = 0; i < token.length(); i += chunkSize) {
                                    int end = Math.min(token.length(), i + chunkSize);
                                    String chunk = token.substring(i, end);
                                    emitter.send(SseEmitter.event().data(chunk));
                                    // 极短的休眠，给网关和前端缓冲区一点点呼吸的时间，但不至于让用户感觉慢
                                    Thread.sleep(2); 
                                }
                            } else {
                                // 正常的短token流式输出
                                emitter.send(SseEmitter.event().data(token));
                            }
                        } catch (IOException | InterruptedException e) {
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
                    } finally {
                        // 清理 ThreadLocal，防止内存泄漏和串话
                        FeignConfig.USER_COOKIE_THREAD_LOCAL.remove();
                    }
                })
                .onError(error -> {
                    // 统一异常处理
                    logger.error("LangChain4j 内部处理流时发生异常: ", error);
                    handleStreamError(emitter, new Exception(error), msg);
                    FeignConfig.USER_COOKIE_THREAD_LOCAL.remove();
                })
                .start();  // 必须调用start()启动异步请求
            
        } catch (Exception e) {
            // 统一异常处理
            handleStreamError(emitter, e, msg);
            FeignConfig.USER_COOKIE_THREAD_LOCAL.remove();
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