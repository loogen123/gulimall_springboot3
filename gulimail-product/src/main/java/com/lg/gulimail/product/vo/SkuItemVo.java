package com.lg.gulimail.product.vo;

import com.lg.gulimail.product.entity.SkuImagesEntity;
import com.lg.gulimail.product.entity.SkuInfoEntity;
import com.lg.gulimail.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {

    // 1、sku基本信息获取  pms_sku_info
    private SkuInfoEntity info;

    // 是否有货（库存信息，由远程调用仓储服务获取）
    private boolean hasStock = true;

    // 2、sku的图片信息    pms_sku_images
    private List<SkuImagesEntity> images;

    // 3、获取spu的销售属性组合
    private List<SkuItemSaleAttrVo> saleAttr;

    // 4、获取spu的介绍（长图描述） pms_spu_info_desc
    private SpuInfoDescEntity desc;

    // 5、获取spu的规格参数信息
    private List<SpuItemAttrGroupVo> groupAttrs;

    private SeckillInfoVo seckillInfo; // 增加秒杀信息
}