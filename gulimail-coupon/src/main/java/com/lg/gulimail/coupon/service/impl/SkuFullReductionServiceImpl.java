package com.lg.gulimail.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.to.SkuReductionTo;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.gulimail.coupon.dao.SkuFullReductionDao;
import com.lg.gulimail.coupon.entity.MemberPriceEntity;
import com.lg.gulimail.coupon.entity.SkuFullReductionEntity;
import com.lg.gulimail.coupon.entity.SkuLadderEntity;
import com.lg.gulimail.coupon.service.MemberPriceService;
import com.lg.gulimail.coupon.service.SkuFullReductionService;
import com.lg.gulimail.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Transactional
    @Override
    public void saveSkuReduction(SkuReductionTo reductionTo) {
        if (reductionTo == null) {
            return;
        }
        // 1. 保存满减信息 (sms_sku_full_reduction)
        SkuFullReductionEntity fullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(reductionTo, fullReductionEntity);
        // 只有满减价格大于0才保存
        if (fullReductionEntity.getFullPrice() != null
                && fullReductionEntity.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
            this.save(fullReductionEntity);
        }

        // 2. 保存打折/阶梯价格信息 (sms_sku_ladder)
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(reductionTo, skuLadderEntity);
        skuLadderEntity.setAddOther(reductionTo.getCountStatus()); // 状态对齐
        // 只有打折满件数大于0才保存
        if (reductionTo.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }

        // 3. 保存会员价格信息 (sms_member_price)
        List<SkuReductionTo.MemberPrice> memberPrices = reductionTo.getMemberPrice();
        if (memberPrices != null && !memberPrices.isEmpty()) {
            List<MemberPriceEntity> collect = memberPrices.stream().map(item -> {
                MemberPriceEntity priceEntity = new MemberPriceEntity();
                priceEntity.setSkuId(reductionTo.getSkuId());
                priceEntity.setMemberLevelId(item.getId());
                priceEntity.setMemberLevelName(item.getName());
                priceEntity.setMemberPrice(item.getPrice());
                priceEntity.setAddOther(1); // 默认叠加优惠
                return priceEntity;
            }).filter(item -> {
                // 过滤掉没填价格的会员价
                return item.getMemberPrice() != null && item.getMemberPrice().compareTo(BigDecimal.ZERO) > 0;
            }).collect(Collectors.toList());

            if (!collect.isEmpty()) {
                memberPriceService.saveBatch(collect);
            }
        }
    }

}
