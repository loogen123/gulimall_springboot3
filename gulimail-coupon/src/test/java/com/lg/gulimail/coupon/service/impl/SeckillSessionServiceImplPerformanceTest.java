package com.lg.gulimail.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lg.gulimail.coupon.entity.SeckillSessionEntity;
import com.lg.gulimail.coupon.entity.SeckillSkuRelationEntity;
import com.lg.gulimail.coupon.service.SeckillSkuRelationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeckillSessionServiceImplPerformanceTest {
    @Mock
    private SeckillSkuRelationService seckillSkuRelationService;

    @Spy
    @InjectMocks
    private SeckillSessionServiceImpl seckillSessionService;

    @Test
    void shouldCompleteSessionAggregationWithinThreshold() {
        SeckillSessionEntity sessionEntity = new SeckillSessionEntity();
        sessionEntity.setId(1L);
        SeckillSkuRelationEntity relationEntity = new SeckillSkuRelationEntity();
        relationEntity.setPromotionSessionId(1L);
        doReturn(List.of(sessionEntity)).when(seckillSessionService).list((Wrapper<SeckillSessionEntity>) any(Wrapper.class));
        when(seckillSkuRelationService.list((Wrapper<SeckillSkuRelationEntity>) any(Wrapper.class)))
                .thenReturn(List.of(relationEntity));

        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            seckillSessionService.getLatest3DaysSession();
        }
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis <= 3000, "秒杀场次聚合耗时过高: " + elapsedMillis + "ms");
    }
}
