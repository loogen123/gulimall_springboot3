package com.lg.gulimail.product.vo;

import lombok.Data;

@Data
public class AttrVo {
    private Long attrId;
    private String attrName;
    private Integer searchType;
    private Integer valueType;
    private String icon;
    private String valueSelect;
    private Integer attrType;
    private Long enable;
    private Long catelogId;
    private Integer showDesc;
    /**
     * 新增字段：所属分组ID
     */
    private Long attrGroupId;
}