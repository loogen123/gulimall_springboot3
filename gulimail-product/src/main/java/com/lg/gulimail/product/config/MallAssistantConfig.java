package com.lg.gulimail.product.config;

import com.lg.gulimail.product.ai.MallAssistant;
import com.lg.gulimail.product.ai.ProductAiTools;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MallAssistantConfig {

    private static final Logger logger = LoggerFactory.getLogger(MallAssistantConfig.class);

    @Bean
    public MallAssistant mallAssistant(@Autowired OpenAiChatModel openAiChatModel,
                                      @Autowired OpenAiStreamingChatModel openAiStreamingChatModel,
                                      ProductAiTools tools) {
        
        logger.info("MallAssistant AI服务初始化开始，使用Nacos配置的模型");
        
        // 打通工具逻辑：使用Spring Boot Starter自动生成的模型
        MallAssistant assistant = AiServices.builder(MallAssistant.class)
            .chatLanguageModel(openAiChatModel)  // 注入Nacos配置的同步模型
            .streamingChatLanguageModel(openAiStreamingChatModel)  // 注入Nacos配置的流式模型
            .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
            .tools(tools)  // 绑定工具
            .build();
            
        logger.info("MallAssistant AI服务初始化成功，已绑定工具");
        
        return assistant;
    }
}