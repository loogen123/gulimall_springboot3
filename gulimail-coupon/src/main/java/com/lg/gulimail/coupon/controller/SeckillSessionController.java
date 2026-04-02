package com.lg.gulimail.coupon.controller;

import com.lg.common.utils.PageUtils;
import com.lg.common.utils.R;
import com.lg.gulimail.coupon.entity.SeckillSessionEntity;
import com.lg.gulimail.coupon.service.SeckillSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("coupon/seckillsession")
public class SeckillSessionController {

    @Autowired
    private SeckillSessionService seckillSessionService;

    /**
     * 秒杀服务远程调用：获取最近三天的秒杀场次
     */
    @GetMapping("/getLatest3DaysSession")
    public R getLatest3DaysSession() {
        List<SeckillSessionEntity> sessions = seckillSessionService.getLatest3DaysSession();
        return R.ok().setData(sessions);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:seckillsession:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = seckillSessionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:seckillsession:info")
    public R info(@PathVariable("id") Long id){
        SeckillSessionEntity seckillSession = seckillSessionService.getById(id);

        return R.ok().put("seckillSession", seckillSession);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:seckillsession:save")
    public R save(@RequestBody SeckillSessionEntity seckillSession){
        if (seckillSession == null) {
            return R.error(10001, "请求参数不能为空");
        }
        seckillSessionService.save(seckillSession);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:seckillsession:update")
    public R update(@RequestBody SeckillSessionEntity seckillSession){
        if (seckillSession == null || seckillSession.getId() == null) {
            return R.error(10001, "请求参数不能为空");
        }
        seckillSessionService.updateById(seckillSession);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:seckillsession:delete")
    public R delete(@RequestBody Long[] ids){
        if (ids == null || ids.length == 0) {
            return R.error(10001, "ids不能为空");
        }
        seckillSessionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }
}
