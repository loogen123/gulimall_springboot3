package com.lg.gulimail.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lg.gulimail.product.entity.AttrGroupEntity;
import com.lg.gulimail.product.vo.SpuItemAttrGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-10 15:21:43
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catelogId") Long catelogId);
}
