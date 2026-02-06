package com.lg.gulimail.product.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SeckillInfoVo {
    private Long id;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer seckillCount;
    private Integer seckillLimit;
    private Integer seckillSort;

    // 关键展示字段
    private String skuTitle;
    private String skuDefaultImg;

    // 状态控制字段
    private String randomCode; // 随机码
    private Long startTime;    // 秒杀开始时间
    private Long endTime;      // 秒杀结束时间
}