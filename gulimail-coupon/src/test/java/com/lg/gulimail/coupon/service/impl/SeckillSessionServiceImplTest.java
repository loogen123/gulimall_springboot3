package com.lg.gulimail.coupon.service.impl;

import com.lg.gulimail.coupon.entity.SeckillSessionEntity;
import com.lg.gulimail.coupon.entity.SeckillSkuRelationEntity;
import com.lg.gulimail.coupon.service.SeckillSkuRelationService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeckillSessionServiceImplTest {

    @Mock
    private SeckillSkuRelationService seckillSkuRelationService;

    @Spy
    @InjectMocks
    private SeckillSessionServiceImpl seckillSessionService;

    @Test
    void getLatest3DaysSessionShouldReturnEmptyWhenNoSession() {
        doReturn(Collections.emptyList()).when(seckillSessionService).list((Wrapper<SeckillSessionEntity>) any(Wrapper.class));
        List<SeckillSessionEntity> result = seckillSessionService.getLatest3DaysSession();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getLatest3DaysSessionShouldAttachRelationsBySession() {
        SeckillSessionEntity s1 = new SeckillSessionEntity();
        s1.setId(1L);
        SeckillSessionEntity s2 = new SeckillSessionEntity();
        s2.setId(2L);
        doReturn(List.of(s1, s2)).when(seckillSessionService).list((Wrapper<SeckillSessionEntity>) any(Wrapper.class));

        SeckillSkuRelationEntity r1 = new SeckillSkuRelationEntity();
        r1.setPromotionSessionId(1L);
        SeckillSkuRelationEntity r2 = new SeckillSkuRelationEntity();
        r2.setPromotionSessionId(1L);
        when(seckillSkuRelationService.list((Wrapper<SeckillSkuRelationEntity>) any(Wrapper.class))).thenReturn(List.of(r1, r2));

        List<SeckillSessionEntity> result = seckillSessionService.getLatest3DaysSession();

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getRelationEntities().size());
        assertEquals(0, result.get(1).getRelationEntities().size());
    }
}
