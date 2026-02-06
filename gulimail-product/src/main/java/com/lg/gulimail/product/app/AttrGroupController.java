package com.lg.gulimail.product.app;

import com.lg.common.utils.PageUtils;
import com.lg.common.utils.R;
import com.lg.gulimail.product.entity.AttrEntity;
import com.lg.gulimail.product.entity.AttrGroupEntity;
import com.lg.gulimail.product.service.AttrGroupService;
import com.lg.gulimail.product.service.AttrService;
import com.lg.gulimail.product.vo.AttrGroupRelationVo;
import com.lg.gulimail.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 属性分组
 *
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-10 15:21:43
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private AttrService attrService;
    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId){
        // 将 catelogId 也传入查询方法中
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }
    @GetMapping("/{attrGroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrGroupId") Long attrGroupId) {
        // 调用 AttrService 获取该分组下的所有属性
        List<AttrEntity> entities = attrService.getRelationAttr(attrGroupId);
        return R.ok().put("data", entities);
    }
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId){
        // 1. 查出当前分类下的所有属性分组
        // 2. 查出每个分组下的所有属性
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data", vos);
    }
    /**
     * 移除属性与分组的关联关系（批量）
     * 路径：POST /product/attrgroup/attr/relation/delete
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos) {
        attrService.deleteRelation(vos);
        return R.ok();
    }
    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        // 1. 获取当前分组所属的三级分类 ID
        Long catelogId = attrGroup.getCatelogId();
        // 2. 调用 Service 方法查出完整路径 [一级, 二级, 三级]
        // 注意：这个方法需要在 AttrGroupService 中定义并实现
        Long[] path = attrGroupService.findCatelogPath(catelogId);
        // 3. 将路径设置到实体类中，返回给前端
        attrGroup.setCatelogPath(path);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * 1. 获取当前分组可以关联的属性列表 (用于“新建关联”弹窗的分页展示)
     * 逻辑：必须是当前分类下的属性，且没有被任何分组关联
     * 路径：GET /product/attrgroup/{attrGroupId}/noattr/relation
     */
    @GetMapping("/{attrGroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrGroupId") Long attrGroupId,
                            @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.getNoRelationAttr(params, attrGroupId);
        // 关键：这里必须 put("page", page)，因为前端在找 data.page
        return R.ok().put("page", page);
    }

    /**
     * 2. 添加属性与分组的关联关系 (用于弹窗点击“确认新增”后的批量保存)
     * 路径：POST /product/attrgroup/attr/relation
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos) {
        // 批量保存关联关系到 pms_attr_attrgroup_relation 表
        attrGroupService.addRelation(vos);
        return R.ok();
    }
}
