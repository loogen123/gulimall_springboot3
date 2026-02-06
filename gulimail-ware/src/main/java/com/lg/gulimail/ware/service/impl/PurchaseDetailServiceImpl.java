package com.lg.gulimail.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.gulimail.ware.dao.PurchaseDetailDao;
import com.lg.gulimail.ware.entity.PurchaseDetailEntity;
import com.lg.gulimail.ware.service.PurchaseDetailService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> queryWrapper = new QueryWrapper<>();

        // 1. 关键字模糊查询 (匹配采购需求的 ID 或 SKU_ID)
        String key = (String) params.get("key");
        if (StringUtils.hasText(key)) {
            // SQL: AND (id = key OR sku_id = key)
            queryWrapper.and(w -> {
                w.eq("id", key).or().eq("sku_id", key);
            });
        }

        // 2. 状态查询
        String status = (String) params.get("status");
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }

        // 3. 仓库 ID 查询
        String wareId = (String) params.get("wareId");
        if (StringUtils.hasText(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long purchaseId) {
        return this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", purchaseId));
    }

}