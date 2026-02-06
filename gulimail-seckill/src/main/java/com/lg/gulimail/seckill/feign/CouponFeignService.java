package com.lg.gulimail.seckill.feign;

import com.lg.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("gulimail-coupon") // 必须和 Nacos 中注册的优惠券服务名一致
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/getLatest3DaysSession")
    R getLatest3DaysSession();
}