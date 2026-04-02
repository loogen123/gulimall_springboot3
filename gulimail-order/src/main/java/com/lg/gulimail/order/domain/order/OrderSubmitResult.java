package com.lg.gulimail.order.domain.order;

import com.lg.gulimail.order.vo.SubmitOrderResponseVo;
import lombok.Data;

@Data
public class OrderSubmitResult {
    public static final int SUCCESS = 0;
    public static final int TOKEN_EXPIRED = 1;
    public static final int PRICE_CHANGED = 2;
    private Integer code;
    private String orderSn;

    public static OrderSubmitResult from(SubmitOrderResponseVo responseVo) {
        OrderSubmitResult result = new OrderSubmitResult();
        if (responseVo == null) {
            result.setCode(TOKEN_EXPIRED);
            return result;
        }
        result.setCode(responseVo.getCode());
        if (responseVo.getOrder() != null) {
            result.setOrderSn(responseVo.getOrder().getOrderSn());
        }
        return result;
    }

    public boolean isSuccess() {
        return SUCCESS == normalizeCode();
    }

    public int normalizeCode() {
        return code == null ? TOKEN_EXPIRED : code;
    }

    public String message() {
        int normalizedCode = normalizeCode();
        if (normalizedCode == TOKEN_EXPIRED) {
            return "订单信息过期，请刷新后再提交";
        }
        if (normalizedCode == PRICE_CHANGED) {
            return "订单商品价格发生变化，请确认后再次提交";
        }
        return "下单失败";
    }
}
