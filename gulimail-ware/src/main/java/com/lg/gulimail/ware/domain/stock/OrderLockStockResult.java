package com.lg.gulimail.ware.domain.stock;

import com.lg.common.exception.BizCodeEnum;
import lombok.Data;

@Data
public class OrderLockStockResult {
    private int code;
    private String message;

    public static OrderLockStockResult success() {
        OrderLockStockResult result = new OrderLockStockResult();
        result.setCode(0);
        result.setMessage("ok");
        return result;
    }

    public static OrderLockStockResult invalidParam() {
        OrderLockStockResult result = new OrderLockStockResult();
        result.setCode(BizCodeEnum.VAILD_EXCEPTION.getCode());
        result.setMessage("请求参数不能为空");
        return result;
    }

    public static OrderLockStockResult noStock() {
        OrderLockStockResult result = new OrderLockStockResult();
        result.setCode(BizCodeEnum.NO_STOCK_EXCEPTION.getCode());
        result.setMessage(BizCodeEnum.NO_STOCK_EXCEPTION.getMsg());
        return result;
    }

    public boolean isSuccess() {
        return code == 0;
    }
}
