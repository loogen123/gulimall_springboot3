package com.lg.gulimail.product.vo;

import lombok.Data;

import java.util.List;

@Data
    public class SpuItemAttrGroupVo {
        private String groupName;
        private List<SpuBaseAttrVo> attrs;
    }