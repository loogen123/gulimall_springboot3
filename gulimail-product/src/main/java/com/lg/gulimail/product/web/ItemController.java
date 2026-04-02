package com.lg.gulimail.product.web;

import com.lg.gulimail.product.application.item.SkuItemApplicationService;
import com.lg.gulimail.product.domain.item.SkuItemResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @Autowired
    private SkuItemApplicationService skuItemApplicationService;

    /**
     * 展示当前 sku 的详情
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) {
        SkuItemResult result = skuItemApplicationService.queryItem(skuId);
        model.addAttribute("item", result.getItem());
        return "item";
    }
}
