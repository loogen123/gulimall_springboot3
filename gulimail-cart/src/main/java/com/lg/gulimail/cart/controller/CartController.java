package com.lg.gulimail.cart.controller;

import com.lg.common.exception.BizCodeEnum;
import com.lg.common.utils.R;
import com.lg.gulimail.cart.application.cart.CartSelectionApplicationService;
import com.lg.gulimail.cart.domain.cart.CartSelectionResult;
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
    private CartSelectionApplicationService cartSelectionApplicationService;

    /**
     * 给远程服务调用的接口
     */
    @GetMapping("/currentUserCartItems")
    public List<CartItem> getCurrentUserCartItems() {
        return cartSelectionApplicationService.getCurrentUserCheckedItems();
    }

    @GetMapping("/checkItem")
    @ResponseBody // 关键：返回 JSON 而不是跳转页面
    public R checkItem(@RequestParam("skuId") Long skuId,
                       @RequestParam("check") Integer check) {
        CartSelectionResult result = cartSelectionApplicationService.checkItem(skuId, check);
        if (!result.isSuccess()) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), result.getMessage());
        }
        return R.ok();
    }
}
