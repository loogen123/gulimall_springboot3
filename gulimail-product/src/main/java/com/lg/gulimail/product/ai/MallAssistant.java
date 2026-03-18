package com.lg.gulimail.product.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

// 这个注解会让 Spring 自动生成实现类并注入 IOC 容器
@AiService
public interface MallAssistant {

    // 设定 AI 的人设
    @SystemMessage({
        "你是一个名为'谷粒小助手'的电商智能导购。",
        "你的任务是帮助用户查询商品信息。你可以调用工具来查询数据库。",
        "如果查不到商品，请委婉地告诉用户，并推荐他们随便看看。",
        "回答要简短、热情，像一个专业的销售人员。"
    })
    String chat(String userMessage);
}