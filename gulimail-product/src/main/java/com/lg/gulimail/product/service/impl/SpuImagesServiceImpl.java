package com.lg.gulimail.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.gulimail.product.dao.SpuImagesDao;
import com.lg.gulimail.product.entity.SpuImagesEntity;
import com.lg.gulimail.product.service.SpuImagesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("spuImagesService")
public class SpuImagesServiceImpl extends ServiceImpl<SpuImagesDao, SpuImagesEntity> implements SpuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuImagesEntity> page = this.page(
                new Query<SpuImagesEntity>().getPage(params),
                new QueryWrapper<SpuImagesEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存 SPU 的图片集：pms_spu_images
     */
    @Override
    public void saveImages(Long id, List<String> images) {
        if (images == null || images.size() == 0) {
            // 如果没有图片，直接返回，不进行操作
            return;
        } else {
            // 将图片 URL 集合转换为实体类集合
            List<SpuImagesEntity> collect = images.stream().map(img -> {
                SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                spuImagesEntity.setSpuId(id);
                spuImagesEntity.setImgUrl(img);
                // 这里通常可以设置一个默认值，比如默认不是封面图
                // spuImagesEntity.setDefaultImg(0);
                return spuImagesEntity;
            }).collect(Collectors.toList());

            // 批量保存到数据库
            this.saveBatch(collect);
        }
    }

}