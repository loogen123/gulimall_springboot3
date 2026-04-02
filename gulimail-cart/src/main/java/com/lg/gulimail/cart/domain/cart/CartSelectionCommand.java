package com.lg.gulimail.cart.domain.cart;

import lombok.Data;

@Data
public class CartSelectionCommand {
    private Long skuId;
    private Integer check;

    public static CartSelectionCommand of(Long skuId, Integer check) {
        CartSelectionCommand command = new CartSelectionCommand();
        command.setSkuId(skuId);
        command.setCheck(check);
        return command;
    }
}
