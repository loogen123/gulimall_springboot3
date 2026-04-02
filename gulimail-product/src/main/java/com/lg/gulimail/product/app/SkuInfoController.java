package com.lg.gulimail.product.app;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.lg.common.exception.BizCodeEnum;
import com.lg.gulimail.product.application.item.SkuItemApplicationService;
import com.lg.gulimail.product.domain.item.SkuItemResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lg.gulimail.product.entity.SkuInfoEntity;
import com.lg.gulimail.product.service.SkuInfoService;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.R;



/**
 * sku信息
 *
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-10 15:21:43
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuItemApplicationService skuItemApplicationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:skuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{skuId}")
    //@RequiresPermissions("product:skuinfo:info")
    public R info(@PathVariable("skuId") Long skuId){
        if (skuId == null || skuId < 1) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "skuId参数非法");
        }
		SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        return R.ok().put("skuInfo", skuInfo);
    }

    /**
     * AI及其他服务获取完整商品详情（包含图片、属性等）
     */
    @RequestMapping("/api/item/{skuId}")
    public R getSkuItem(@PathVariable("skuId") Long skuId){
        SkuItemResult result = skuItemApplicationService.queryItem(skuId);
        if (!result.isSuccess()) {
            return R.error(result.getCode(), result.getMessage());
        }
        return R.ok().put("data", result.getItem());
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:skuinfo:save")
    public R save(@RequestBody SkuInfoEntity skuInfo){
        if (skuInfo == null) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "请求参数不能为空");
        }
		skuInfoService.save(skuInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:skuinfo:update")
    public R update(@RequestBody SkuInfoEntity skuInfo){
        if (skuInfo == null || skuInfo.getSkuId() == null) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "请求参数不能为空");
        }
		skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:skuinfo:delete")
    public R delete(@RequestBody Long[] skuIds){
        if (skuIds == null || skuIds.length == 0) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "skuIds不能为空");
        }
		skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }

}
