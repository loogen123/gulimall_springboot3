package com.lg.gulimail.seckill.controller;

import com.lg.common.utils.R;
import com.lg.gulimail.seckill.service.SeckillService;
import com.lg.gulimail.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller // 注意：如果需要跳转页面用@Controller，纯接口用@RestController
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 1. 获取当前时间正在参与秒杀的商品信息
     * 用于首页/秒杀频道页展示
     */
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SeckillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    /**
     * 2. 获取指定 SKU 的秒杀预告信息
     * 用于商品详情页（item.gulimail.com）展示秒杀状态
     */
    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    /**
     * 3. 核心：执行秒杀下单逻辑
     * @param killId   场次ID_商品ID (如：1_45)
     * @param key      随机码 (Redis中的randomCode)
     * @param num      购买数量
     */
    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model) {

        String orderSn = seckillService.kill(killId, key, num);

        if (StringUtils.hasText(orderSn)) {
            model.addAttribute("orderSn", orderSn);
            return "success"; // 跳到刚才那个成功的页面
        } else {
            // 核心改动：跳到失败页面，并带上错误信息
            model.addAttribute("msg", "由于参与人数过多，请稍后再试（可能库存不足或您已参与过该活动）");
            return "fail";
        }
    }


}