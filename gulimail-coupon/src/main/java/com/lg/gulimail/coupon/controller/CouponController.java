package com.lg.gulimail.coupon.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lg.common.exception.BizCodeEnum;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.R;
import com.lg.gulimail.coupon.entity.CouponEntity;
import com.lg.gulimail.coupon.entity.CouponHistoryEntity;
import com.lg.gulimail.coupon.service.CouponHistoryService;
import com.lg.gulimail.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 优惠券信息
 *
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-04 18:08:23
 */
@RestController
@RequestMapping("coupon/coupon")
public class CouponController {
    @Autowired
    private CouponService couponService;
    @Autowired
    private CouponHistoryService couponHistoryService;

    /**
     * 列表
     */
    @Value("${coupon.user.name}")
    private String name;
    @Value("${coupon.user.age}")
    private Integer age;
    @RequestMapping("/test")
    public R test(){
        return R.ok().put("coupons", Arrays.asList("满100减10")).put("name", name).put("age", age);
    }
    @RequestMapping("/member/list")
    public R memberCoupons(){
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setCouponName("满100减10");
        return R.ok().put("coupons",  Arrays.asList(couponEntity));
    }
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:coupon:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = couponService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:coupon:info")
    public R info(@PathVariable("id") Long id){
		CouponEntity coupon = couponService.getById(id);

        return R.ok().put("coupon", coupon);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:coupon:save")
    public R save(@RequestBody CouponEntity coupon){
        if (coupon == null) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "请求参数不能为空");
        }
		couponService.save(coupon);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:coupon:update")
    public R update(@RequestBody CouponEntity coupon){
        if (coupon == null || coupon.getId() == null) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "请求参数不能为空");
        }
		couponService.updateById(coupon);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:coupon:delete")
    public R delete(@RequestBody Long[] ids){
        if (ids == null || ids.length == 0) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "ids不能为空");
        }
		couponService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @PostMapping("/internal/deduct")
    public R deductCoupon(@RequestBody Map<String, Object> request) {
        Long memberId = parseLong(request == null ? null : request.get("memberId"));
        Long couponId = parseLong(request == null ? null : request.get("couponId"));
        Long orderId = parseLong(request == null ? null : request.get("orderId"));
        Long orderSn = parseLong(request == null ? null : request.get("orderSn"));
        if (memberId == null || couponId == null || orderSn == null) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "请求参数不合法");
        }
        CouponHistoryEntity history = couponHistoryService.getOne(
                new QueryWrapper<CouponHistoryEntity>()
                        .eq("member_id", memberId)
                        .eq("coupon_id", couponId)
                        .eq("order_sn", orderSn)
                        .last("limit 1")
        );
        if (history != null && history.getUseType() != null && history.getUseType() == 1) {
            return R.ok();
        }
        if (history == null) {
            history = couponHistoryService.getOne(
                    new QueryWrapper<CouponHistoryEntity>()
                            .eq("member_id", memberId)
                            .eq("coupon_id", couponId)
                            .eq("use_type", 0)
                            .last("limit 1")
            );
        }
        if (history == null) {
            return R.error(BizCodeEnum.NOT_FOUND_EXCEPTION.getCode(), "未找到可核销优惠券记录");
        }
        history.setUseType(1);
        history.setUseTime(new java.util.Date());
        history.setOrderId(orderId);
        history.setOrderSn(orderSn);
        couponHistoryService.updateById(history);
        CouponEntity coupon = couponService.getById(couponId);
        if (coupon != null) {
            int useCount = coupon.getUseCount() == null ? 0 : coupon.getUseCount();
            coupon.setUseCount(useCount + 1);
            couponService.updateById(coupon);
        }
        return R.ok();
    }

    @PostMapping("/internal/revert")
    public R revertCoupon(@RequestBody Map<String, Object> request) {
        Long memberId = parseLong(request == null ? null : request.get("memberId"));
        Long couponId = parseLong(request == null ? null : request.get("couponId"));
        Long orderSn = parseLong(request == null ? null : request.get("orderSn"));
        if (memberId == null || couponId == null || orderSn == null) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "请求参数不合法");
        }
        CouponHistoryEntity history = couponHistoryService.getOne(
                new QueryWrapper<CouponHistoryEntity>()
                        .eq("member_id", memberId)
                        .eq("coupon_id", couponId)
                        .eq("order_sn", orderSn)
                        .last("limit 1")
        );
        if (history == null || history.getUseType() == null || history.getUseType() != 1) {
            return R.ok();
        }
        history.setUseType(0);
        history.setUseTime(null);
        history.setOrderId(null);
        history.setOrderSn(null);
        couponHistoryService.updateById(history);
        CouponEntity coupon = couponService.getById(couponId);
        if (coupon != null) {
            int useCount = coupon.getUseCount() == null ? 0 : coupon.getUseCount();
            coupon.setUseCount(Math.max(useCount - 1, 0));
            couponService.updateById(coupon);
        }
        return R.ok();
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
