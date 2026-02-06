package com.lg.gulimail.seckill.to;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SeckillSkuRedisTo {
    private Long id;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer seckillCount;
    private Integer seckillLimit;
    private Integer seckillSort;

    // --- 已经有的额外字段 ---
    private String randomCode; // 随机码
    private Long startTime;    // 秒杀开始时间
    private Long endTime;      // 秒杀结束时间

    // --- 【新增：必须添加以下两个字段，前端才能拿到数据】 ---
    private String skuTitle;      // 商品标题
    private String skuDefaultImg; // 商品默认图片
}