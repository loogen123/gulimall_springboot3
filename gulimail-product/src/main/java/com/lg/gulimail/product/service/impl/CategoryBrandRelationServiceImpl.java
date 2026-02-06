package com.lg.gulimail.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.gulimail.product.dao.BrandDao;
import com.lg.gulimail.product.dao.CategoryBrandRelationDao;
import com.lg.gulimail.product.dao.CategoryDao;
import com.lg.gulimail.product.entity.BrandEntity;
import com.lg.gulimail.product.entity.CategoryBrandRelationEntity;
import com.lg.gulimail.product.entity.CategoryEntity;
import com.lg.gulimail.product.service.BrandService;
import com.lg.gulimail.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    @Autowired
    BrandDao brandDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationDao relationDao;

    @Lazy
    @Autowired
    BrandService brandService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();

        // 1. 查询品牌的详细信息
        BrandEntity brandEntity = brandDao.selectById(brandId);
        // 2. 查询分类的详细信息
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        // 3. 将名称设置进关联实体类中
        if (brandEntity != null) {
            categoryBrandRelation.setBrandName(brandEntity.getName());
        }
        if (categoryEntity != null) {
            categoryBrandRelation.setCatelogName(categoryEntity.getName());
        }

        // 4. 保存到数据库
        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        relationEntity.setBrandName(name);
        relationEntity.setBrandId(brandId);

        // 构造更新条件：WHERE brand_id = ?
        this.update(relationEntity,
                new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId)
        );
    }

    @Override
    public void updateCategory(Long catId, String name) {
        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        relationEntity.setCatelogName(name);
        relationEntity.setCatelogId(catId);

        // 构造更新条件：WHERE catelog_id = catId
        this.update(relationEntity,
                new UpdateWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId)
        );
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        // 1. 查出所有关联关系
        List<CategoryBrandRelationEntity> relations = relationDao.selectList(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));

        // 2. 收集所有 BrandId
        List<Long> brandIds = relations.stream()
                .map(CategoryBrandRelationEntity::getBrandId)
                .collect(Collectors.toList());

        if (brandIds.isEmpty()) {
            return null;
        }

        // 3. 一次性查出所有品牌信息（性能提升 10 倍以上）
        return brandService.listByIds(brandIds);
    }

}