package com.lg.gulimail.order.feign;

import com.lg.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient("gulimail-coupon")
public interface CouponFeignService {
    @GetMapping("/coupon/coupon/info/{id}")
    R getCouponInfo(@PathVariable("id") Long id);

    @PostMapping("/coupon/coupon/internal/deduct")
    R deductCoupon(@RequestBody Map<String, Object> request);

    @PostMapping("/coupon/coupon/internal/revert")
    R revertCoupon(@RequestBody Map<String, Object> request);
}
