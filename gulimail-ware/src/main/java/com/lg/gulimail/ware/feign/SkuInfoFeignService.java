package com.lg.gulimail.ware.feign;

import com.lg.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

// 填写商品微服务在 Nacos 中注册的服务名
@FeignClient("gulimail-product")
public interface SkuInfoFeignService {

    /**
     * 远程调用商品服务：获取 sku 的详细信息
     * 对应商品服务中 SkuInfoController 的 info 方法
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}