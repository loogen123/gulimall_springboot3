package com.lg.gulimail.product.feign;

import com.lg.common.utils.R;
import com.lg.gulimail.product.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 订单微服务Feign接口
 */
@FeignClient(value = "gulimail-order", configuration = FeignConfig.class)
public interface OrderFeignService {

    /**
     * 查询当前用户的订单列表（包含订单项）
     * 注意：由于底层实现依赖 LoginUserInterceptor，
     * 所以在发起请求时需要确保 Header 中带有用户的 token/cookie。
     * 否则 LoginUserInterceptor 无法获取当前登录用户。
     */
    @PostMapping("/order/order/listWithItem")
    R listWithItem(@RequestBody Map<String, Object> params);

}
