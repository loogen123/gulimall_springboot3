package com.lg.gulimail.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lg.gulimail.product.entity.AttrAttrgroupRelationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-10 15:21:43
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    // 必须加 @Param("entities")，名字要与 XML 中的 collection 一致
    void deleteBatchRelation(@Param("entities") List<AttrAttrgroupRelationEntity> entities);
}