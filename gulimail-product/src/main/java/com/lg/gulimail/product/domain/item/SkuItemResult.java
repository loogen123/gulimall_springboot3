package com.lg.gulimail.product.domain.item;

import com.lg.common.exception.BizCodeEnum;
import com.lg.gulimail.product.vo.SkuItemVo;
import lombok.Data;

@Data
public class SkuItemResult {
    private Integer code;
    private String message;
    private SkuItemVo item;

    public static SkuItemResult success(SkuItemVo item) {
        SkuItemResult result = new SkuItemResult();
        result.setCode(0);
        result.setMessage("success");
        result.setItem(item);
        return result;
    }

    public static SkuItemResult invalidSkuId() {
        SkuItemResult result = new SkuItemResult();
        result.setCode(BizCodeEnum.VAILD_EXCEPTION.getCode());
        result.setMessage("skuId参数非法");
        return result;
    }

    public boolean isSuccess() {
        return code != null && code == 0;
    }
}
