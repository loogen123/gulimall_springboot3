package com.lg.gulimail.product.vo;

import lombok.Data;

@Data
public class AttrRespVo extends AttrVo {
    /**
     * "手机"
     */
    private String catelogName;
    /**
     * "主体"
     */
    private String groupName;
    /**
     * 分类完整路径 [2, 34, 225] 用于前端级联选择器回显
     */
    private Long[] catelogPath;
}