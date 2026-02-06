package com.lg.gulimail.cart.service;

import com.lg.gulimail.cart.vo.Cart;
import com.lg.gulimail.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    // 添加商品
    CartItem addToCart(Long skuId, Integer num, Long userId) throws ExecutionException, InterruptedException;
    // 获取单个项
    CartItem getCartItem(Long skuId, Long userId);
    // 获取全量购物车
    Cart getCart(Long userId) throws ExecutionException, InterruptedException;

    void deleteItem(Long skuId, Long userId);

    List<CartItem> getUserCartItems();

    void checkItem(Long skuId, Integer check);
}