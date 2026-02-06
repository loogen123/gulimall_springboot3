package com.lg.gulimail.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.constant.ProductConstant;
import com.lg.common.to.SkuReductionTo;
import com.lg.common.to.SpuBoundTo;
import com.lg.common.to.es.SkuEsModel;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.common.utils.R;
import com.lg.common.vo.SkuHasStockVo;
import com.lg.gulimail.product.dao.SpuInfoDao;
import com.lg.gulimail.product.entity.*;
import com.lg.gulimail.product.feign.CouponFeignService;
import com.lg.gulimail.product.feign.SearchFeignService;
import com.lg.gulimail.product.feign.WareFeignService;
import com.lg.gulimail.product.service.*;
import com.lg.gulimail.product.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SpuImagesService spuImagesService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private ProductAttrValueService attrValueService;
    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SearchFeignService searchFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }
    @Transactional // 涉及到多表操作，必须加事务
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        // 1. 保存 SPU 基本信息：pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);

        // 2. 保存 SPU 的描述图片：pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        // 3. 保存 SPU 的图片集：pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(infoEntity.getId(), images);

        // 4. 保存 SPU 的规格参数：pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getQuickShow());
            valueEntity.setSpuId(infoEntity.getId());
            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(collect);

        // 5. 保存 SPU 的积分信息：gulimail_sms -> sms_spu_bounds (跨服务调用)
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存SPU积分信息失败");
        }

        // 6. 保存当前 SPU 对应的所有 SKU 信息：
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                // 6.1 SKU的基本信息：pms_sku_info
                String defaultImg = "";
                for (SkuImages image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.save(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                // 6.2 SKU的图片信息：pms_sku_images
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> !StringUtils.isEmpty(entity.getImgUrl())).collect(Collectors.toList());
                skuImagesService.saveBatch(imagesEntities);

                // 6.3 SKU的销售属性信息：pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);
                    return attrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // 6.4 SKU的优惠、满减等信息：gulimail_sms -> sms_sku_ladder \ sms_sku_full_reduction \ sms_member_price (跨服务)
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存SKU优惠信息失败");
                    }
                }
            });
        }
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        // 1. 获取检索关键字 key (模糊查询: id 或 spu_name)
        String key = (String) params.get("key");
        if (StringUtils.hasText(key)) {
            // SELECT * FROM pms_spu_info WHERE (id = key OR spu_name LIKE %key%)
            wrapper.and((w) -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }

        // 2. 获取分类ID (catalogId)
        String catalogId = (String) params.get("catalogId");
        if (StringUtils.hasText(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            wrapper.eq("catalog_id", catalogId);
        }

        // 3. 获取品牌ID (brandId)
        String brandId = (String) params.get("brandId");
        if (StringUtils.hasText(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        // 4. 获取上架状态 (status)
        String status = (String) params.get("status");
        if (StringUtils.hasText(status)) {
            wrapper.eq("publish_status", status);
        }

        // 5. 执行分页查询
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        // 1. 查出当前 spuId 对应的所有 sku 信息
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        if (skus == null || skus.isEmpty()) {
            log.warn("SPU: {} 下无 SKU 记录，终止上架", spuId);
            return;
        }
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 2. 查询当前 SPU 的所有【可检索】的基础属性 (逻辑保持不变)
        List<ProductAttrValueEntity> baseAttrs = attrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream()
                .filter(item -> idSet.contains(item.getAttrId()))
                .map(item -> {
                    SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attrs1);
                    return attrs1;
                }).collect(Collectors.toList());

        // 3. 远程调用库存系统，获取库存 Map
        Map<Long, Boolean> stockMap = null;
        try {
            R r = wareFeignService.getSkusHasStock(skuIdList);
            log.info("库存服务返回的原始 R 数据: {}", r);
            if (r.getCode() == 0) {
                stockMap = r.getData(new TypeReference<List<SkuHasStockVo>>(){}).stream()
                        .collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
            }
        } catch (Exception e) {
            log.error("库存服务查询异常：{}", e);
        }

        // 4. 封装 SKU 信息，并【过滤】掉没有库存的 SKU
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts = skus.stream()
                .filter(sku -> {
                    // --- 核心修改：检查是否有库存 ---
                    // 如果库存 Map 为空，可能库存服务宕机，为了不影响业务，通常默认有货(true)或跳过(false)
                    // 这里建议：如果查不到库存信息，为了保险起见，暂不参与过滤，或者根据你的业务定
                    if (finalStockMap == null) {
                        return true;
                    }
                    return finalStockMap.getOrDefault(sku.getSkuId(), false); // 只有库存为 true 的才留下
                })
                .map(sku -> {
                    SkuEsModel esModel = new SkuEsModel();
                    BeanUtils.copyProperties(sku, esModel);
                    esModel.setSkuPrice(sku.getPrice());
                    esModel.setSkuImg(sku.getSkuDefaultImg());
                    esModel.setHasStock(true); // 既然能经过上面的 filter，这里肯定是有货的
                    esModel.setHotScore(0L);

                    // 查询品牌/分类名 (逻辑保持不变)
                    BrandEntity brand = brandService.getById(esModel.getBrandId());
                    esModel.setBrandName(brand.getName());
                    esModel.setBrandImg(brand.getLogo());
                    CategoryEntity category = categoryService.getById(esModel.getCatalogId());
                    esModel.setCatalogName(category.getName());
                    esModel.setAttrs(attrsList);
                    return esModel;
                }).collect(Collectors.toList());

        // --- 5. 发送前进行终极检查 ---
        if (upProducts.isEmpty()) {
            log.warn("SPU: {} 下的所有 SKU 均无库存，放弃同步至 ES", spuId);
            // 注意：即使 ES 不用同步，你也可以决定是否要把数据库状态改为上架。
            // 通常没货不能在前端搜到，所以这里直接 return
            return;
        }

        // 5.1 发送数据给 ES
        try {
            R r = searchFeignService.productStatusUp(upProducts);
            if (r.getCode() == 0) {
                baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
                log.info("SPU: {} 上架成功，同步 SKU 数量：{}", spuId, upProducts.size());
            } else {
                log.error("SPU: {} 上架失败，ES 服务报错", spuId);
            }
        } catch (Exception e) {
            log.error("SPU: {} 上架异常，网络通讯失败", spuId);
            e.printStackTrace();
        }
    }

    private void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        // 这里的 this 指的是 SpuInfoServiceImpl
        // MyBatis-Plus 的 save 方法执行完后，会自动将自增 ID 回填到 infoEntity 的 id 属性中
        this.baseMapper.insert(infoEntity);
    }

}