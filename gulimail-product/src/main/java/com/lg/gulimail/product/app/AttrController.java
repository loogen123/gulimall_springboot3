package com.lg.gulimail.product.app;

import com.lg.common.utils.PageUtils;
import com.lg.common.utils.R;
import com.lg.gulimail.product.entity.ProductAttrValueEntity;
import com.lg.gulimail.product.service.AttrService;
import com.lg.gulimail.product.service.ProductAttrValueService;
import com.lg.gulimail.product.vo.AttrRespVo;
import com.lg.gulimail.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品属性
 *
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-10 15:21:43
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    /**
     * 获取分类下的规格参数（基本属性）
     * 路径：/product/attr/base/list/{catelogId}
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String type){

        // type 如果是 "base"，则查询规格参数（attr_type=1）
        // type 如果是 "sale"，则查询销售属性（attr_type=0）
        PageUtils page = attrService.queryBaseAttrPage(params, catelogId, type);
        return R.ok().put("page", page);
    }
    /**
     * 获取 spu 的规格参数
     * /product/attr/base/listforspu/{spuId}
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R listForSpu(@PathVariable("spuId") Long spuId){
        // 调用 ProductAttrValueService 来查询，因为规格参数值存在这张表里
        List<ProductAttrValueEntity> entities = productAttrValueService.listforSpu(spuId);

        return R.ok().put("data", entities);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
        // 修改：不再直接查实体，而是调用 service 获取封装好的 VO
        AttrRespVo respVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", respVo);
    }

    /**
     * 保存
     */
// 保存
    @PostMapping("/save")
    public R save(@RequestBody AttrVo attr){
        attrService.saveAttr(attr); // 调用新增的方法
        return R.ok();
    }

    // 修改
    @PostMapping("/update")
    public R update(@RequestBody AttrVo attr){
        attrService.updateAttr(attr); // 调用新增的方法
        return R.ok();
    }
    /**
     * 修改 SPU 规格参数
     * 路径：/product/attr/update/{spuId}
     */
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities){

        productAttrValueService.updateSpuAttr(spuId, entities);
        return R.ok();
    }
    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
