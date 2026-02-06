package com.lg.gulimail.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lg.common.utils.PageUtils;
import com.lg.gulimail.coupon.entity.SeckillSessionEntity;
import java.util.List;
import java.util.Map;

public interface SeckillSessionService extends IService<SeckillSessionEntity> {
    /**
     * 获取最近三天的秒杀场次及关联商品
     */
    List<SeckillSessionEntity> getLatest3DaysSession();

    PageUtils queryPage(Map<String, Object> params);
}