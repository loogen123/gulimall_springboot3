package com.lg.common.vo;

import lombok.Data;

@Data
public class SkuInfoVo {
    private Long skuId;
    private String skuName; // 对应商品名称
    private String skuDefaultImg; // 对应默认图片
    // 其他字段根据需要添加
}