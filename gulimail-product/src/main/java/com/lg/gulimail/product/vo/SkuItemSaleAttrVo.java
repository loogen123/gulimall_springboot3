package com.lg.gulimail.product.vo;

import lombok.Data;

import java.util.List;

@Data
    public class SkuItemSaleAttrVo {
        private Long attrId;
        private String attrName;
        private List<AttrValueWithSkuIdVo> attrValues;
    }