package com.lg.gulimail.ai.feign;

import com.lg.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("gulimail-product")
public interface ProductFeignService {

    @GetMapping("/product/skuinfo/api/item/{skuId}")
    R getSkuItem(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skuinfo/list")
    R queryPageByCondition(@RequestParam Map<String, Object> params);

    @GetMapping("/product/category/list/tree")
    R getAllCategories();

    @GetMapping("/product/category/api/level1")
    R getLevel1Categories();

    @GetMapping("/product/brand/info/{brandId}")
    R getBrandInfo(@PathVariable("brandId") Long brandId);
}
