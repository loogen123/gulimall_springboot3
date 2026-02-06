package com.lg.gulimail.order.feign;

import com.lg.common.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("gulimail-cart") // 必须对应 Nacos 中购物车服务的名字
public interface CartFeignService {

    /**
     * 获取当前用户购物车中选中的商品项
     */
    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();
}