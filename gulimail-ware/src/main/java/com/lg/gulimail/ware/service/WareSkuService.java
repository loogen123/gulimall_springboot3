package com.lg.gulimail.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lg.common.to.OrderTo;
import com.lg.common.utils.PageUtils;
import com.lg.common.vo.SkuHasStockVo;
import com.lg.gulimail.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-04 22:52:15
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    Boolean orderLockStock(com.lg.common.to.mq.WareSkuLockTo vo);

    void unlockStock(OrderTo orderTo);

    void orderDeductStock(String orderSn);
}

