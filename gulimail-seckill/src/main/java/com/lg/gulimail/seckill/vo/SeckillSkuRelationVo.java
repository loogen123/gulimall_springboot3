package com.lg.gulimail.seckill.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SeckillSkuRelationVo {
    private Long id;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice; // 秒杀价
    private Integer seckillCount;   // 秒杀总量
    private Integer seckillLimit;   // 限购额度
    private Integer seckillSort;
}