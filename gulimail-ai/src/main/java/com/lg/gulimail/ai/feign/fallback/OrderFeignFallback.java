package com.lg.gulimail.ai.feign.fallback;

import com.lg.common.utils.R;
import com.lg.gulimail.ai.feign.OrderFeignService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderFeignFallback implements OrderFeignService {
    @Override
    public R listWithItem(Map<String, Object> params) {
        return R.error(503, "订单查询服务暂时不可用，请稍后再试（熔断降级）");
    }
}
