package com.lg.gulimail.order.controller;

import com.lg.common.utils.PageUtils;
import com.lg.common.utils.R;
import com.lg.gulimail.order.entity.OrderEntity;
import com.lg.gulimail.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 订单
 *
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-04 22:29:44
 */
@RestController
@RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * AI查询当前用户的订单列表（供微服务内部Feign调用）
     */
    @PostMapping("/listWithItem")
    public R listWithItem(@RequestBody Map<String, Object> params){
        try {
            // 如果内部调用没有传递用户信息，这里可能会抛出异常
            PageUtils page = orderService.queryPageWithItem(params);
            return R.ok().put("page", page);
        } catch (Exception e) {
            // 捕获异常，比如未登录时 LoginUserInterceptor.loginUser.get() 可能返回 null
            return R.error(401, "未登录或获取用户信息失败");
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("order:order:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("order:order:info")
    public R info(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("order:order:save")
    public R save(@RequestBody OrderEntity order){
		orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("order:order:update")
    public R update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("order:order:delete")
    public R delete(@RequestBody Long[] ids){
		orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }
}
