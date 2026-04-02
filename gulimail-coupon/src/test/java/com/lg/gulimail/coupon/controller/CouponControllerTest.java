package com.lg.gulimail.coupon.controller;

import com.lg.common.utils.R;
import com.lg.gulimail.coupon.entity.CouponEntity;
import com.lg.gulimail.coupon.service.CouponService;
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
class CouponControllerTest {

    @Mock
    private CouponService couponService;

    @InjectMocks
    private CouponController couponController;

    @Test
    void saveShouldRejectWhenBodyNull() {
        R result = couponController.save(null);

        assertEquals(10001, result.getCode());
        verify(couponService, never()).save(null);
    }

    @Test
    void updateShouldRejectWhenIdNull() {
        CouponEntity couponEntity = new CouponEntity();
        R result = couponController.update(couponEntity);

        assertEquals(10001, result.getCode());
        verify(couponService, never()).updateById(couponEntity);
    }

    @Test
    void deleteShouldRejectWhenIdsEmpty() {
        R result = couponController.delete(new Long[0]);

        assertEquals(10001, result.getCode());
        verifyNoInteractions(couponService);
    }
}
