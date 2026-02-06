package com.lg.gulimail.product.vo;

import lombok.Data;

/**
 * 属性分组关联关系的VO
 * 用于新增关联和移除关联时接收前端传来的数据
 */
@Data
public class AttrGroupRelationVo {

    /**
     * 属性id
     */
    private Long attrId;

    /**
     * 属性分组id
     */
    private Long attrGroupId;

}