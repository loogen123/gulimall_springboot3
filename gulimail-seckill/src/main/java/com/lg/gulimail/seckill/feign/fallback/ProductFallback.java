package com.lg.gulimail.seckill.feign.fallback;

import com.lg.common.utils.R;
import com.lg.gulimail.seckill.feign.ProductFeignService;
import org.springframework.stereotype.Component;

@Component
public class ProductFallback implements ProductFeignService {
    @Override
    public R getSkuInfo(Long skuId) {
        // 这里的逻辑就是“降级”后的补偿
        return R.error(10002, "商品服务暂时不可用，我们正在拼命修复...");
    }
}