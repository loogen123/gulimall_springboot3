package com.lg.gulimail.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.constant.WareConstant;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.gulimail.ware.dao.PurchaseDao;
import com.lg.gulimail.ware.entity.PurchaseDetailEntity;
import com.lg.gulimail.ware.entity.PurchaseEntity;
import com.lg.gulimail.ware.service.PurchaseDetailService;
import com.lg.gulimail.ware.service.PurchaseService;
import com.lg.gulimail.ware.service.WareSkuService;
import com.lg.gulimail.ware.vo.MergeVo;
import com.lg.gulimail.ware.vo.PurchaseDoneVo;
import com.lg.gulimail.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    private PurchaseDetailService purchaseDetailService;
    @Autowired
    private WareSkuService wareSkuService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();

        // 1. 限定采购单状态：只能查“新建” (0) 或 “已分配” (1) 的采购单
        // SQL: WHERE (status = 0 OR status = 1)
        queryWrapper.in("status", 0, 1);

        // 2. 既然是“未领取”，通常这些单子的采购人 ID 可能是空的，或者我们需要模糊搜索
        String key = (String) params.get("key");
        if (StringUtils.hasText(key)) {
            // 如果有关键字，搜索采购单 ID
            queryWrapper.eq("id", key);
        }

        // 3. 执行分页
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();

        // 1. 如果没有勾选现有的采购单，则新建一个
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        // 2. 确认采购单状态是 0 或 1 才能合并 (业务校验)
        PurchaseEntity purchaseEntity = this.getById(purchaseId);
        if (purchaseEntity.getStatus() != WareConstant.PurchaseStatusEnum.CREATED.getCode() &&
                purchaseEntity.getStatus() != WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
            // 只有新建或已分配的采购单才能合并
            return;
        }

        // 3. 更新采购需求的采购单 ID 和 状态
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;

        // 过滤出符合要求的采购需求（状态必须是 0 或 1）
        // 先查出所有的需求单
        Collection<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listByIds(items);

        List<PurchaseDetailEntity> collect = purchaseDetailEntities.stream()
                .filter(item -> {
                    // 只有状态为新建(0)或已分配(1)的采购需求才能被合并
                    return item.getStatus() == WareConstant.PurchaseDetailStatusEnum.CREATED.getCode() ||
                            item.getStatus() == WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode();
                })
                .map(item -> {
                    PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                    detailEntity.setId(item.getId());
                    detailEntity.setPurchaseId(finalPurchaseId);
                    detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                    return detailEntity;
                }).collect(Collectors.toList());

        if (collect.size() > 0) {
            purchaseDetailService.updateBatchById(collect);

            // 4. 更新采购单的更新时间
            PurchaseEntity updatePurchase = new PurchaseEntity();
            updatePurchase.setId(finalPurchaseId);
            updatePurchase.setUpdateTime(new Date());
            this.updateById(updatePurchase);
        }
    }

    @Transactional
    @Override
    public void receivePurchase(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Date now = new Date();
        List<PurchaseEntity> collect = this.listByIds(ids).stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                        || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
                .map(item -> {
                    item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                    item.setUpdateTime(now);
                    return item;
                }).collect(Collectors.toList());

        if (collect.isEmpty()) {
            return;
        }
        this.updateBatchById(collect);

        List<Long> purchaseIds = collect.stream().map(PurchaseEntity::getId).collect(Collectors.toList());
        List<PurchaseDetailEntity> detailEntities = purchaseDetailService.listDetailByPurchaseIds(purchaseIds);
        if (detailEntities.isEmpty()) {
            return;
        }
        List<PurchaseDetailEntity> detailCollect = detailEntities.stream().map(entity -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(entity.getId());
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
            return detailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(detailCollect);
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {
        Long id = doneVo.getId();
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        if (items == null || items.isEmpty()) {
            return;
        }

        Boolean flag = true;
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        List<Long> itemIds = items.stream().map(PurchaseItemDoneVo::getItemId).collect(Collectors.toList());
        Map<Long, PurchaseDetailEntity> detailMap = purchaseDetailService.listByIds(itemIds).stream()
                .collect(Collectors.toMap(PurchaseDetailEntity::getId, item -> item));

        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
                detailEntity.setStatus(item.getStatus());
            } else {
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                PurchaseDetailEntity entity = detailMap.get(item.getItemId());
                if (entity != null) {
                    wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
                }
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }
        purchaseDetailService.updateBatchById(updates);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}
