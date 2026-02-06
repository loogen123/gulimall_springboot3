package com.lg.gulimail.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SpuSaveVo {
    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private BigDecimal weight;
    private int publishStatus;
    private List<String> decript; // 商品详情图
    private List<String> images;  // 商品图集
    private Bounds bounds;        // 积分信息
    private List<BaseAttrs> baseAttrs; // 规格参数
    private List<Skus> skus;      // SKU列表
}