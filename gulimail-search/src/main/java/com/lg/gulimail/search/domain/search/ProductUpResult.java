package com.lg.gulimail.search.domain.search;

import com.lg.common.exception.BizCodeEnum;
import lombok.Data;

@Data
public class ProductUpResult {
    private Integer code;
    private String message;

    public static ProductUpResult success() {
        ProductUpResult result = new ProductUpResult();
        result.setCode(0);
        result.setMessage("success");
        return result;
    }

    public static ProductUpResult invalidParam() {
        ProductUpResult result = new ProductUpResult();
        result.setCode(BizCodeEnum.VAILD_EXCEPTION.getCode());
        result.setMessage("上架商品列表不能为空");
        return result;
    }

    public static ProductUpResult failed() {
        ProductUpResult result = new ProductUpResult();
        result.setCode(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode());
        result.setMessage(BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        return result;
    }

    public boolean isSuccess() {
        return code != null && code == 0;
    }
}
