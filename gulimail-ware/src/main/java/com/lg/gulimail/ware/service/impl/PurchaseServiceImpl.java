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
        // 1. 确认当前采购单是“新建”或“已分配”状态才可以领取
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            return this.getById(id);
        }).filter(item -> {
            // 只有 状态为 0 (新建) 或 1 (已分配) 的采购单才能领取
            return item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode();
        }).map(item -> {
            // 2. 改变采购单状态为“已领取” (2)
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        // 3. 批量更新采购单表
        this.updateBatchById(collect);

        // 4. 改变该采购单下所有需求单（Detail）的状态
        collect.forEach(item -> {
            // 找到该采购单对应的所有需求项，将状态改为“正在采购” (2)
            List<PurchaseDetailEntity> detailEntities = purchaseDetailService.listDetailByPurchaseId(item.getId());

            List<PurchaseDetailEntity> detailCollect = detailEntities.stream().map(entity -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                detailEntity.setId(entity.getId());
                // 状态改为 2 (正在采购)
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return detailEntity;
            }).collect(Collectors.toList());

            purchaseDetailService.updateBatchById(detailCollect);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {
        Long id = doneVo.getId();

        // 1. 改变采购项(Detail)的状态
        Boolean flag = true;
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();

        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
                detailEntity.setStatus(item.getStatus());
            } else {
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                // 3. 将成功采购的商品进行入库
                // 查出当前采购项的详细信息（为了获取skuId, num, wareId）
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }
        purchaseDetailService.updateBatchById(updates);

        // 2. 改变采购单(Purchase)的状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}