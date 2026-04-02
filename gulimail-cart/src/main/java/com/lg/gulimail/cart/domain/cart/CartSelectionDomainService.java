package com.lg.gulimail.cart.domain.cart;

import org.springframework.stereotype.Service;

@Service
public class CartSelectionDomainService {
    public CartSelectionCommand normalize(CartSelectionCommand command) {
        if (command == null) {
            return new CartSelectionCommand();
        }
        return command;
    }

    public CartSelectionResult validate(CartSelectionCommand command) {
        if (command.getSkuId() == null || command.getSkuId() < 1) {
            return CartSelectionResult.invalid("skuId参数非法");
        }
        if (command.getCheck() == null || (command.getCheck() != 0 && command.getCheck() != 1)) {
            return CartSelectionResult.invalid("check参数非法");
        }
        return CartSelectionResult.success();
    }
}
