package com.lg.gulimail.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lg.gulimail.product.entity.AttrEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-10 15:21:43
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> selectSearchAttrIds(@Param("attrIds") List<Long> attrIds);
}
