package com.lg.gulimail.cart.infrastructure.cart;

import com.lg.gulimail.cart.application.port.out.CartSelectionPort;
import com.lg.gulimail.cart.service.CartService;
import com.lg.gulimail.cart.vo.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartSelectionPortAdapter implements CartSelectionPort {
    private final CartService cartService;

    public CartSelectionPortAdapter(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public List<CartItem> getCurrentUserCheckedItems() {
        return cartService.getUserCartItems();
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        cartService.checkItem(skuId, check);
    }
}
