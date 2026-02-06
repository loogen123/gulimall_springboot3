package com.lg.gulimail.product.vo;

import lombok.Data;

@Data
    public class AttrValueWithSkuIdVo {
        private String attrValue;
        private String skuIds; // 该属性值对应的所有 skuId 集合，方便前端切换
    }