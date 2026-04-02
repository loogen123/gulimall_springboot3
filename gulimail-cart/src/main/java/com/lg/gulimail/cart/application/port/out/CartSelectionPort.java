package com.lg.gulimail.cart.application.port.out;

import com.lg.gulimail.cart.vo.CartItem;

import java.util.List;

public interface CartSelectionPort {
    List<CartItem> getCurrentUserCheckedItems();

    void checkItem(Long skuId, Integer check);
}
