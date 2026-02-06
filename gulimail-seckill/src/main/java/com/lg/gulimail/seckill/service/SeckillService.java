package com.lg.gulimail.seckill.service;

import com.lg.gulimail.seckill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SeckillService {
    /**
     * 上架三天内需要秒杀的商品
     */
    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    /**
     * 获取指定SKU的秒杀信息
     */
    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    /**
     * 秒杀下单逻辑
     */
    String kill(String killId, String key, Integer num);

}