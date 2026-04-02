package com.lg.gulimail.seckill.application.port.out;

import com.lg.gulimail.seckill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SeckillSkuQueryPort {
    List<SeckillSkuRedisTo> listCurrentSkus();

    SeckillSkuRedisTo getSkuInfo(Long skuId);
}
