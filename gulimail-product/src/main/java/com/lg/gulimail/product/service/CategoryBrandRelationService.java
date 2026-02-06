package com.lg.gulimail.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lg.common.utils.PageUtils;
import com.lg.common.valid.AddGroup;
import com.lg.common.valid.UpdateGroup;
import com.lg.gulimail.product.entity.BrandEntity;
import com.lg.gulimail.product.entity.CategoryBrandRelationEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-10 15:21:43
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(@NotNull(message = "修改必须指定品牌id", groups = {UpdateGroup.class}) @Null(message = "新增不能指定id", groups = {AddGroup.class}) Long brandId, @NotBlank(message = "品牌名不能为空", groups = {AddGroup.class, UpdateGroup.class}) String name);

    void updateCategory(Long catId, String name);

    List<BrandEntity> getBrandsByCatId(Long catId);
}

