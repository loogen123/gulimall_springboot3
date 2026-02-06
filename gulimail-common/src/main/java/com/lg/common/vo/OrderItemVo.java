package com.lg.common.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {
    private Long skuId;
    private String title;
    private String image;
    private List<String> skuAttr; // 商品销售属性
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;

    // 还需要重量等信息可以继续添加
    private Long wareId;
}