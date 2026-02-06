package com.lg.gulimail.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.exception.NoStockException;
import com.lg.common.to.OrderTo;
import com.lg.common.to.mq.WareSkuLockTo;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.common.utils.R;
import com.lg.common.vo.OrderItemVo;
import com.lg.common.vo.SkuHasStockVo;
import com.lg.gulimail.ware.dao.WareSkuDao;
import com.lg.gulimail.ware.entity.WareOrderTaskDetailEntity;
import com.lg.gulimail.ware.entity.WareOrderTaskEntity;
import com.lg.gulimail.ware.entity.WareSkuEntity;
import com.lg.gulimail.ware.feign.SkuInfoFeignService;
import com.lg.gulimail.ware.service.WareOrderTaskDetailService;
import com.lg.gulimail.ware.service.WareOrderTaskService;
import com.lg.gulimail.ware.service.WareSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    private WareSkuDao wareSkuDao;
    @Autowired
    private SkuInfoFeignService skuInfoFeignService;
    @Autowired
    private WareOrderTaskService wareOrderTaskService;
    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();

        // 1. 获取 skuId (注意：数据库字段名是 sku_id)
        String skuId = (String) params.get("skuId");
        if (StringUtils.hasText(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        // 2. 获取 wareId (注意：数据库字段名是 ware_id)
        String wareId = (String) params.get("wareId");
        if (StringUtils.hasText(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 1. 判断如果还没有这个库存记录，则新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>()
                .eq("sku_id", skuId).eq("ware_id", wareId));

        if (entities == null || entities.size() == 0) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            // 1、自己catch异常
            try {
                R info = skuInfoFeignService.info(skuId);
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
                // 记录日志或不处理
            }
            wareSkuDao.insert(skuEntity);
        } else {
            // 2. 如果有记录，则累加库存
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        return skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count > 0);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean orderLockStock(WareSkuLockTo vo) {
        // 【新增】1. 保存库存工作单主表 (wms_ware_order_task)
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        taskEntity.setCreateTime(new Date());
        wareOrderTaskService.save(taskEntity);

        List<OrderItemVo> locks = vo.getLocks();
        for (OrderItemVo item : locks) {
            List<Long> wareIds = baseMapper.listWareIdsHasSkuStock(item.getSkuId(), item.getCount());
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(item.getSkuId());
            }

            boolean skuStocked = false;
            for (Long wareId : wareIds) {
                Long count = baseMapper.lockSkuStock(item.getSkuId(), wareId, item.getCount());
                if (count == 1) {
                    // 【新增】锁定成功，保存锁定详情记录 (wms_ware_order_task_detail)
                    WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
                    detailEntity.setSkuId(item.getSkuId());
                    detailEntity.setSkuNum(item.getCount());
                    detailEntity.setTaskId(taskEntity.getId());
                    detailEntity.setWareId(wareId); // 存入具体的仓库ID
                    detailEntity.setLockStatus(1);  // 状态设置为 1-已锁定
                    wareOrderTaskDetailService.save(detailEntity);

                    skuStocked = true;
                    break;
                }
            }

            if (!skuStocked) {
                throw new NoStockException(item.getSkuId());
            }
        }
        return true;
    }

    @Override
    @Transactional
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();

        // 1. 根据订单号查询“库存工作单”
        // 在锁定库存时，你应该已经在 wms_ware_order_task 表里存了单号
        WareOrderTaskEntity task = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);

        if (task != null) {
            // 2. 找到该工作单下所有“锁定”状态的工作单项 (task_detail)
            List<WareOrderTaskDetailEntity> details = wareOrderTaskDetailService.list(
                    new QueryWrapper<WareOrderTaskDetailEntity>()
                            .eq("task_id", task.getId())
                            .eq("lock_status", 1) // 1-已锁定，2-已解锁，3-已扣减
            );

            for (WareOrderTaskDetailEntity detail : details) {
                // 3. 真正的数据库操作：解冻库存
                // UPDATE wms_ware_sku SET stock_locked = stock_locked - count
                // WHERE sku_id = ? AND ware_id = ?
                unLockSql(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detail.getId());
            }
        }
    }

    @Transactional
    @Override
    public void orderDeductStock(String orderSn) {
        System.out.println("====== [开始执行物理扣减] 订单号: " + orderSn + " ======");

        // 1. 查找该订单对应的库存工作单
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        if (taskEntity == null) {
            System.out.println("错误：未找到订单 [" + orderSn + "] 对应的库存工作单！");
            return;
        }
        Long taskId = taskEntity.getId();

        // 2. 查找该工作单下所有【已锁定】的详情
        List<WareOrderTaskDetailEntity> details = wareOrderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", taskId)
                        .eq("lock_status", 1)
        );

        if (details == null || details.isEmpty()) {
            System.out.println("提示：订单 [" + orderSn + "] 没有需要扣减的锁定详情。");
            return;
        }

        for (WareOrderTaskDetailEntity detail : details) {
            Long skuId = detail.getSkuId();
            Long wareId = detail.getWareId();
            Integer skuNum = detail.getSkuNum();

            // 检查数据完整性
            if (skuId == null || wareId == null || skuNum == null) {
                System.out.println("错误：详情单据数据不全！ID: " + detail.getId() + "，跳过此项。");
                continue;
            }

            System.out.println("准备扣减库存 -> 商品ID: " + skuId + ", 仓库ID: " + wareId + ", 数量: " + skuNum);

            // 3. 执行物理扣减 SQL
            Long count = baseMapper.realDeductStock(skuId, wareId, skuNum);

            if (count > 0) {
                // 4. 更新详情状态为：已扣减 (2)
                detail.setLockStatus(2);
                wareOrderTaskDetailService.updateById(detail);
                System.out.println("成功：商品 [" + skuId + "] 物理扣减完成。");
            } else {
                System.out.println("严重错误：物理库存扣减失败！SQL返回0，单号: " + orderSn);
                throw new RuntimeException("库存扣减异常，回滚事务");
            }
        }
        System.out.println("====== [物理扣减结束] 订单号: " + orderSn + " ======");
    }

    private void unLockSql(Long skuId, Long wareId, Integer num, Long detailId) {
        // 1. 物理更新：回滚库存锁定数量
        baseMapper.unLockStockSql(skuId, wareId, num);

        // 2. 状态更新：将工作单详情状态改为“已解锁(2)”
        WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
        detailEntity.setId(detailId);
        detailEntity.setLockStatus(2); // 1-已锁定，2-已解锁
        wareOrderTaskDetailService.updateById(detailEntity);
    }
}