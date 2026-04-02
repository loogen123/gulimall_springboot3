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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public List<SeckillSessionEntity> getLatest3DaysSession() {
        String startTime = LocalDateTime.now().with(LocalTime.MIN).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endTime = LocalDateTime.now().plusDays(2).with(LocalTime.MAX).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<SeckillSessionEntity> sessions = this.list(new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", startTime, endTime));

        if (sessions == null || sessions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> sessionIds = sessions.stream().map(SeckillSessionEntity::getId).collect(Collectors.toList());
        List<SeckillSkuRelationEntity> relationEntities = seckillSkuRelationService.list(
                new QueryWrapper<SeckillSkuRelationEntity>().in("promotion_session_id", sessionIds)
        );
        Map<Long, List<SeckillSkuRelationEntity>> relationMap = relationEntities.stream()
                .collect(Collectors.groupingBy(SeckillSkuRelationEntity::getPromotionSessionId));
        sessions.forEach(session -> session.setRelationEntities(
                relationMap.getOrDefault(session.getId(), Collections.emptyList())
        ));
        return sessions;
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
