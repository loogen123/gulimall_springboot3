package com.lg.gulimail.coupon.controller;

import com.lg.common.utils.R;
import com.lg.gulimail.coupon.entity.CouponHistoryEntity;
import com.lg.gulimail.coupon.service.CouponHistoryService;
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
class CouponHistoryControllerTest {

    @Mock
    private CouponHistoryService couponHistoryService;

    @InjectMocks
    private CouponHistoryController couponHistoryController;

    @Test
    void saveShouldRejectWhenBodyNull() {
        R result = couponHistoryController.save(null);

        assertEquals(10001, result.getCode());
        verify(couponHistoryService, never()).save(null);
    }

    @Test
    void updateShouldRejectWhenIdNull() {
        CouponHistoryEntity entity = new CouponHistoryEntity();
        R result = couponHistoryController.update(entity);

        assertEquals(10001, result.getCode());
        verify(couponHistoryService, never()).updateById(entity);
    }

    @Test
    void deleteShouldRejectWhenIdsEmpty() {
        R result = couponHistoryController.delete(new Long[0]);

        assertEquals(10001, result.getCode());
        verifyNoInteractions(couponHistoryService);
    }
}
