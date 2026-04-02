package com.lg.gulimail.cart.domain.cart;

import com.lg.common.exception.BizCodeEnum;
import lombok.Data;

@Data
public class CartSelectionResult {
    private int code;
    private String message;

    public static CartSelectionResult success() {
        CartSelectionResult result = new CartSelectionResult();
        result.setCode(0);
        result.setMessage("ok");
        return result;
    }

    public static CartSelectionResult invalid(String message) {
        CartSelectionResult result = new CartSelectionResult();
        result.setCode(BizCodeEnum.VAILD_EXCEPTION.getCode());
        result.setMessage(message);
        return result;
    }

    public boolean isSuccess() {
        return code == 0;
    }
}
