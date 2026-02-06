package com.lg.gulimail.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lg.gulimail.ware.entity.WareSkuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-04 22:52:15
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(Long skuId, Long wareId, Integer skuNum);

    long getSkuStock(Long skuId);

    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("count") Integer count);

    List<Long> listWareIdsHasSkuStock(@Param("skuId") Long skuId, @Param("count") Integer count);

    void unLockStockSql(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    Long realDeductStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);
}
