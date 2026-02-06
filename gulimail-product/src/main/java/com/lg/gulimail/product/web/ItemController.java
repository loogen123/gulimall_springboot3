package com.lg.gulimail.product.web;

import com.lg.gulimail.product.service.SkuInfoService;
import com.lg.gulimail.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    /**
     * 展示当前 sku 的详情
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) {

        System.out.println("准备查询 " + skuId + " 的详情");

        // 调用 Service 查询商品详情数据
        SkuItemVo vo = skuInfoService.item(skuId);

        // 放入 model 中给前端渲染
        model.addAttribute("item", vo);

        return "item";
    }
}