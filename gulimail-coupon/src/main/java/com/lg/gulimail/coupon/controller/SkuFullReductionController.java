package com.lg.gulimail.coupon.controller;

import com.lg.common.utils.PageUtils;
import com.lg.common.utils.R;
import com.lg.gulimail.coupon.entity.SkuFullReductionEntity;
import com.lg.gulimail.coupon.service.SkuFullReductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 商品满减信息
 *
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-04 18:08:23
 */
@RestController
@RequestMapping("coupon/skufullreduction")
public class SkuFullReductionController {
    @Autowired
    private SkuFullReductionService skuFullReductionService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:skufullreduction:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuFullReductionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:skufullreduction:info")
    public R info(@PathVariable("id") Long id){
        if (id == null || id < 1) {
            return R.error(10001, "id参数非法");
        }
		SkuFullReductionEntity skuFullReduction = skuFullReductionService.getById(id);

        return R.ok().put("skuFullReduction", skuFullReduction);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:skufullreduction:save")
    public R save(@RequestBody SkuFullReductionEntity skuFullReduction){
        if (skuFullReduction == null) {
            return R.error(10001, "请求参数不能为空");
        }
		skuFullReductionService.save(skuFullReduction);

        return R.ok();
    }
    /**
     * 远程调用：保存 SKU 的优惠、满减、打折、会员价等所有信息
     * 路径：/coupon/skufullreduction/saveinfo
     */
    @PostMapping("/saveinfo")
    public R saveinfo(@RequestBody com.lg.common.to.SkuReductionTo skuReductionTo){
        if (skuReductionTo == null || skuReductionTo.getSkuId() == null || skuReductionTo.getSkuId() < 1) {
            return R.error(10001, "skuId参数非法");
        }
        // 调用 Service 层处理多表保存逻辑
        skuFullReductionService.saveSkuReduction(skuReductionTo);
        return R.ok();
    }
    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:skufullreduction:update")
    public R update(@RequestBody SkuFullReductionEntity skuFullReduction){
        if (skuFullReduction == null || skuFullReduction.getId() == null) {
            return R.error(10001, "请求参数不能为空");
        }
		skuFullReductionService.updateById(skuFullReduction);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:skufullreduction:delete")
    public R delete(@RequestBody Long[] ids){
        if (ids == null || ids.length == 0) {
            return R.error(10001, "ids不能为空");
        }
		skuFullReductionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
