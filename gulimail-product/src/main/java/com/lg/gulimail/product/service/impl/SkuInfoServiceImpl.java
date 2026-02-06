package com.lg.gulimail.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.common.utils.R;
import com.lg.gulimail.product.dao.SkuInfoDao;
import com.lg.gulimail.product.entity.SkuImagesEntity;
import com.lg.gulimail.product.entity.SkuInfoEntity;
import com.lg.gulimail.product.entity.SpuInfoDescEntity;
import com.lg.gulimail.product.feign.SeckillFeignService;
import com.lg.gulimail.product.service.*;
import com.lg.gulimail.product.vo.SeckillInfoVo;
import com.lg.gulimail.product.vo.SkuItemSaleAttrVo;
import com.lg.gulimail.product.vo.SkuItemVo;
import com.lg.gulimail.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SeckillFeignService seckillFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();

        // 1. 关键字检索 (sku_id 或 sku_name)
        String key = (String) params.get("key");
        if (StringUtils.hasText(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        // 2. 分类查询
        String catalogId = (String) params.get("catalogId");
        if (StringUtils.hasText(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            queryWrapper.eq("catalog_id", catalogId);
        }

        // 3. 品牌查询
        String brandId = (String) params.get("brandId");
        if (StringUtils.hasText(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        // 4. 价格区间查询 (min - max)
        String min = (String) params.get("min");
        if (StringUtils.hasText(min)) {
            // ge: greater than or equal (>=)
            queryWrapper.ge("price", min);
        }

        String max = (String) params.get("max");
        if (StringUtils.hasText(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                // 只有当最大值大于 0 时才添加条件
                if (bigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                    // le: less than or equal (<=)
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {
                // 忽略非数字格式的 max 参数
            }
        }

        // 5. 分页查询
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        // SELECT * FROM pms_sku_info WHERE spu_id = ?
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();

        // ======= 【核心修改】：获取主线程的请求上下文 =======
        // 必须在异步开启前获取，否则异步开启后主线程可能直接结束或丢失引用
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 1. sku基本信息获取 (异步任务1)
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // 每一个异步子线程都要手动绑定上下文
            RequestContextHolder.setRequestAttributes(requestAttributes);
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, executor);

        // 2. sku图片信息获取
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, executor);

        // 3. 销售属性获取
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);

        // 4. spu介绍获取
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDescEntity);
        }, executor);

        // 5. 规格参数获取
        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, executor);

        // 6. 查询当前 SKU 是否参与秒杀优惠
        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            R r = seckillFeignService.getSkuSeckillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillInfoVo seckillInfoVo = r.getData(new TypeReference<SeckillInfoVo>() {});
                skuItemVo.setSeckillInfo(seckillInfoVo);
            }
        }, executor);

        // 等待所有任务完成
        CompletableFuture.allOf(imageFuture, saleAttrFuture, descFuture, baseAttrFuture, seckillFuture).join();

        return skuItemVo;
    }

}