package com.lg.gulimail.seckill.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.lg.common.exception.BizCodeEnum;
import com.lg.common.utils.R; // 使用你项目里的通用返回对象
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SeckillSentinelConfig implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=utf-8");
        R error = R.error(BizCodeEnum.TOO_MANY_REQUESTS.getCode(), "秒杀太火爆了，请稍后再试！");
        response.getWriter().write(JSON.toJSONString(error));
    }
}
