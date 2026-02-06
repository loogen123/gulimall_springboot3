package com.lg.gulimail.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.gulimail.coupon.dao.SeckillSessionDao;
import com.lg.gulimail.coupon.entity.SeckillSessionEntity;
import com.lg.gulimail.coupon.entity.SeckillSkuRelationEntity;
import com.lg.gulimail.coupon.service.SeckillSessionService;
import com.lg.gulimail.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public List<SeckillSessionEntity> getLatest3DaysSession() {
        // 1. 计算未来三天的起止时间
        // 今天 00:00:00 -> 后天 23:59:59
        String startTime = LocalDateTime.now().with(LocalTime.MIN).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endTime = LocalDateTime.now().plusDays(2).with(LocalTime.MAX).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 2. 查询符合时间范围的秒杀场次
        List<SeckillSessionEntity> sessions = this.list(new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", startTime, endTime));

        if (sessions != null && !sessions.isEmpty()) {
            // 3. 关联查询出每个场次下所有的商品项 (SKU_RELATION)
            return sessions.stream().map(session -> {
                List<SeckillSkuRelationEntity> skus = seckillSkuRelationService.list(
                        new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", session.getId())
                );
                session.setRelationEntities(skus);
                return session;
            }).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSessionEntity> queryWrapper = new QueryWrapper<>();

        // 获取页面传过来的检索关键字 key
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            // 如果有 key，则匹配 ID 或者 场次名称
            queryWrapper.and(w -> w.eq("id", key).or().like("name", key));
        }

        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }
}