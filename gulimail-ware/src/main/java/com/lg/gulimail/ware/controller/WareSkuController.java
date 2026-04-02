package com.lg.gulimail.ware.controller;

import com.lg.common.exception.BizCodeEnum;
import com.lg.common.to.mq.WareSkuLockTo;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.R;
import com.lg.common.vo.SkuHasStockVo;
import com.lg.gulimail.ware.application.stock.OrderLockStockApplicationService;
import com.lg.gulimail.ware.domain.stock.OrderLockStockResult;
import com.lg.gulimail.ware.entity.WareSkuEntity;
import com.lg.gulimail.ware.service.WareSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;



/**
 * 商品库存
 *
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-04 22:52:15
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;
    @Autowired
    private OrderLockStockApplicationService orderLockStockApplicationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }

        // 必须是 Post 方式，因为 Product 模块传的是 List<Long>
        @PostMapping("/hasstock")
        public R getSkusHasStock(@RequestBody List<Long> skuIds) {
            if (skuIds == null || skuIds.isEmpty()) {
                return R.ok().setData(Collections.emptyList());
            }
            List<SkuHasStockVo> vos = wareSkuService.getSkusHasStock(skuIds);
            return R.ok().setData(vos);
        }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
        if (wareSku == null) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "请求参数不能为空");
        }
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
        if (wareSku == null || wareSku.getId() == null) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "请求参数不能为空");
        }
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
        if (ids == null || ids.length == 0) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "ids不能为空");
        }
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }
    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockTo vo) {
        OrderLockStockResult result = orderLockStockApplicationService.lockStock(vo);
        if (result.isSuccess()) {
            return R.ok();
        }
        if (result.getCode() == BizCodeEnum.NO_STOCK_EXCEPTION.getCode()) {
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION);
        }
        return R.error(result.getCode(), result.getMessage());
    }
}
