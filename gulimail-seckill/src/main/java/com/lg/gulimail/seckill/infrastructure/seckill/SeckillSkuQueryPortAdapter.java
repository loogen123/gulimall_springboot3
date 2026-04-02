package com.lg.gulimail.seckill.infrastructure.seckill;

import com.lg.gulimail.seckill.application.port.out.SeckillSkuQueryPort;
import com.lg.gulimail.seckill.service.SeckillService;
import com.lg.gulimail.seckill.to.SeckillSkuRedisTo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeckillSkuQueryPortAdapter implements SeckillSkuQueryPort {
    private final SeckillService seckillService;

    public SeckillSkuQueryPortAdapter(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    @Override
    public List<SeckillSkuRedisTo> listCurrentSkus() {
        return seckillService.getCurrentSeckillSkus();
    }

    @Override
    public SeckillSkuRedisTo getSkuInfo(Long skuId) {
        return seckillService.getSkuSeckillInfo(skuId);
    }
}
