package com.lg.gulimail.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.gulimail.product.dao.SkuSaleAttrValueDao;
import com.lg.gulimail.product.entity.SkuSaleAttrValueEntity;
import com.lg.gulimail.product.service.SkuSaleAttrValueService;
import com.lg.gulimail.product.vo.SkuItemSaleAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {
        SkuSaleAttrValueDao dao = this.baseMapper;
        return dao.getSaleAttrsBySpuId(spuId);
    }

    @Override
    public List<String> getSkuSaleAttrValuesAsStringList(Long skuId) {
        // 这里的逻辑通常是查询 sku_id 对应的 attr_name 和 attr_value
        List<SkuSaleAttrValueEntity> entities = this.list(new QueryWrapper<SkuSaleAttrValueEntity>().eq("sku_id", skuId));

        return entities.stream().map(entity -> {
            return entity.getAttrName() + ": " + entity.getAttrValue();
        }).collect(Collectors.toList());
    }

}