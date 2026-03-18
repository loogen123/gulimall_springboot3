package com.lg.gulimail.product.web;

import com.lg.gulimail.product.ai.MallAssistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product/ai")
@CrossOrigin // 解决前端跨域问题
public class AiChatController {

    @Autowired
    private MallAssistant mallAssistant;

    @GetMapping("/chat")
    public String chat(@RequestParam("msg") String msg) {
        // 直接调用 AI
        return mallAssistant.chat(msg);
    }
}