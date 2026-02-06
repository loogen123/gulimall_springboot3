package com.lg.gulimail.seckill.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.lg.common.utils.R; // 使用你项目里的通用返回对象
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class SeckillSentinelConfig implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        // 1. 设置响应头
        response.setStatus(200); // 虽然被限流了，但我们给前端返回 200，内容里带错误码
        response.setContentType("application/json;charset=utf-8");

        // 2. 封装返回信息
        R error = R.error(10001, "秒杀太火爆了，请稍后再试！");

        // 3. 写回响应
        response.getWriter().write(JSON.toJSONString(error));
    }
}