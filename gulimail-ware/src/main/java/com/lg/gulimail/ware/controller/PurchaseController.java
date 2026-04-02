package com.lg.gulimail.ware.controller;

import com.lg.common.utils.PageUtils;
import com.lg.common.utils.R;
import com.lg.gulimail.ware.entity.PurchaseEntity;
import com.lg.gulimail.ware.service.PurchaseService;
import com.lg.gulimail.ware.vo.MergeVo;
import com.lg.gulimail.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 采购信息
 *
 * @author lll
 * @email lll@gmail.com
 * @date 2026-01-02 22:20:50
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;
    //ware/purchase/unreceive/list
    @RequestMapping("/unreceive/list")
    //@RequiresPermissions("ware:purchase:list")
    public R unreceivelist(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceive(params);

        return R.ok().put("page", page);
    }

    /**
     * 领取采购单
     * /ware/purchase/received
     */
    @PostMapping("/received")
    public R received(@RequestBody List<Long> ids){
        if (ids == null || ids.isEmpty()) {
            return R.error(10001, "ids不能为空");
        }
        purchaseService.receivePurchase(ids);
        return R.ok();
    }
    /**
     * 完成采购单
     * /ware/purchase/received
     */
    @PostMapping("/done")
    public R finish(@RequestBody PurchaseDoneVo doneVo){
        if (doneVo == null || doneVo.getId() == null) {
            return R.error(10001, "请求参数不能为空");
        }
        purchaseService.done(doneVo);
        return R.ok();
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }
    //合并采购单
    @PostMapping("/merge")
    public R merge(@RequestBody MergeVo mergeVo){
        if (mergeVo == null || mergeVo.getItems() == null || mergeVo.getItems().isEmpty()) {
            return R.error(10001, "请求参数不能为空");
        }
        purchaseService.mergePurchase(mergeVo);
        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
        if (purchase == null) {
            return R.error(10001, "请求参数不能为空");
        }
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
        if (purchase == null || purchase.getId() == null) {
            return R.error(10001, "请求参数不能为空");
        }
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
        if (ids == null || ids.length == 0) {
            return R.error(10001, "ids不能为空");
        }
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
