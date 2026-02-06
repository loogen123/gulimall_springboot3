package com.lg.gulimail.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.gulimail.product.dao.BrandDao;
import com.lg.gulimail.product.entity.BrandEntity;
import com.lg.gulimail.product.service.BrandService;
import com.lg.gulimail.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 1. 获取查询关键字
        String key = (String) params.get("key");
        // 2. 构造查询条件
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<>();
        // 如果关键字不为空，则拼接模糊查询条件
        if (!StringUtils.isEmpty(key)) {
            // 逻辑：SELECT * FROM pms_brand WHERE (brand_id = key OR name LIKE %key%)
            queryWrapper.and((wrapper) -> {
                wrapper.eq("brand_id", key).or().like("name", key);
            });
        }

        // 3. 执行查询
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional // 必须开启事务
    @Override
    public void updateDetail(BrandEntity brand) {
        // 1. 更新品牌表自身的基本信息
        this.updateById(brand);

        // 2. 如果品牌名不为空，同步更新关联表中的冗余数据
        if (!StringUtils.isEmpty(brand.getName())) {
            // 同步更新关联表中的品牌名
            categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());

            // TODO: 如果还有其他表冗余了品牌名（例如：spu信息表），也需要在这里同步调用更新
        }
    }

}