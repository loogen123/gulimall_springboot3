package com.lg.common.exception;

public class NoStockException extends RuntimeException {
    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品ID：" + skuId + " 库存不足！");
        this.skuId = skuId;
    }

    public Long getSkuId() {
        return skuId;
    }
}
