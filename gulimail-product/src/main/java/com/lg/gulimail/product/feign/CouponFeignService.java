package com.lg.gulimail.product.feign;

import com.lg.common.to.SkuReductionTo;
import com.lg.common.to.SpuBoundTo;
import com.lg.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @FeignClient("gulimail-coupon") 告诉 SpringCloud 这是一个远程客户端，
 * 目标服务在 Nacos 中注册的名字是 gulimail-coupon
 */
@FeignClient("gulimail-coupon")
public interface CouponFeignService {

    /**
     * 1. 远程保存 SPU 积分信息
     * 对应 coupon 微服务中 SpuBoundsController 的 save 方法
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    /**
     * 2. 远程保存 SKU 优惠、满减信息
     * 对应 coupon 微服务中 SkuFullReductionController 的 saveinfo 方法
     */
    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}