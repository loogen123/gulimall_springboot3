package com.lg.gulimail.cart.controller;

import com.lg.common.utils.R;
import com.lg.gulimail.cart.service.CartService;
import com.lg.gulimail.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // 这里的 @RestController = @Controller + @ResponseBody
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 给远程服务调用的接口
     */
    @GetMapping("/currentUserCartItems")
    public List<CartItem> getCurrentUserCartItems() {
        // 直接返回对象，Spring 会自动转为 JSON
        return cartService.getUserCartItems();
    }

    @GetMapping("/checkItem")
    @ResponseBody // 关键：返回 JSON 而不是跳转页面
    public R checkItem(@RequestParam("skuId") Long skuId,
                       @RequestParam("check") Integer check) {

        cartService.checkItem(skuId, check);

        return R.ok(); // 返回通用的成功标识
    }
}