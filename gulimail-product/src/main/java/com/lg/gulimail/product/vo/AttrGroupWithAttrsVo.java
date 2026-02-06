package com.lg.gulimail.product.vo;

import com.lg.gulimail.product.entity.AttrEntity;
import com.lg.gulimail.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

@Data
public class AttrGroupWithAttrsVo extends AttrGroupEntity {
    // 关键字段：该分组下的所有属性集合
    private List<AttrEntity> attrs;
}