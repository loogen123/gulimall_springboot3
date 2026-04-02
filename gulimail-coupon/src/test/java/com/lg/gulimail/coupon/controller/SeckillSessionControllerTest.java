package com.lg.gulimail.coupon.controller;

import com.lg.common.utils.R;
import com.lg.gulimail.coupon.entity.SeckillSessionEntity;
import com.lg.gulimail.coupon.service.SeckillSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SeckillSessionControllerTest {

    @Mock
    private SeckillSessionService seckillSessionService;

    @InjectMocks
    private SeckillSessionController seckillSessionController;

    @Test
    void saveShouldRejectWhenBodyNull() {
        R result = seckillSessionController.save(null);

        assertEquals(10001, result.getCode());
        verify(seckillSessionService, never()).save(null);
    }

    @Test
    void updateShouldRejectWhenIdNull() {
        SeckillSessionEntity session = new SeckillSessionEntity();
        R result = seckillSessionController.update(session);

        assertEquals(10001, result.getCode());
        verify(seckillSessionService, never()).updateById(session);
    }

    @Test
    void deleteShouldRejectWhenIdsEmpty() {
        R result = seckillSessionController.delete(new Long[0]);

        assertEquals(10001, result.getCode());
        verifyNoInteractions(seckillSessionService);
    }
}
