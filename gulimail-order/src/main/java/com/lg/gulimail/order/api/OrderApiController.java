package com.lg.gulimail.order.api;

import com.lg.common.utils.R;
import com.lg.gulimail.order.application.order.SubmitOrderApplicationService;
import com.lg.gulimail.order.domain.order.OrderSubmitResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order/v1/orders")
public class OrderApiController {
    private final SubmitOrderApplicationService submitOrderApplicationService;

    public OrderApiController(SubmitOrderApplicationService submitOrderApplicationService) {
        this.submitOrderApplicationService = submitOrderApplicationService;
    }

    @PostMapping
    public R submit(@RequestBody OrderSubmitRequest request) {
        OrderSubmitResult result = submitOrderApplicationService.submitOrder(request.toCommand());
        if (result.isSuccess()) {
            return R.ok().put("orderSn", result.getOrderSn());
        }
        return R.error(result.normalizeCode(), result.message());
    }
}
