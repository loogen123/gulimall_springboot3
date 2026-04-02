package com.lg.gulimail.coupon.controller;

import com.lg.common.to.SkuReductionTo;
import com.lg.common.utils.R;
import com.lg.gulimail.coupon.entity.SkuFullReductionEntity;
import com.lg.gulimail.coupon.service.SkuFullReductionService;
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
class SkuFullReductionControllerTest {

    @Mock
    private SkuFullReductionService skuFullReductionService;

    @InjectMocks
    private SkuFullReductionController skuFullReductionController;

    @Test
    void infoShouldRejectWhenIdInvalid() {
        R result = skuFullReductionController.info(0L);

        assertEquals(10001, result.getCode());
        verify(skuFullReductionService, never()).getById(0L);
    }

    @Test
    void saveShouldRejectWhenBodyNull() {
        R result = skuFullReductionController.save(null);

        assertEquals(10001, result.getCode());
        verify(skuFullReductionService, never()).save(null);
    }

    @Test
    void saveinfoShouldRejectWhenSkuIdInvalid() {
        SkuReductionTo skuReductionTo = new SkuReductionTo();

        R result = skuFullReductionController.saveinfo(skuReductionTo);

        assertEquals(10001, result.getCode());
        verify(skuFullReductionService, never()).saveSkuReduction(skuReductionTo);
    }

    @Test
    void updateShouldRejectWhenIdNull() {
        SkuFullReductionEntity entity = new SkuFullReductionEntity();

        R result = skuFullReductionController.update(entity);

        assertEquals(10001, result.getCode());
        verify(skuFullReductionService, never()).updateById(entity);
    }

    @Test
    void deleteShouldRejectWhenIdsEmpty() {
        R result = skuFullReductionController.delete(new Long[0]);

        assertEquals(10001, result.getCode());
        verifyNoInteractions(skuFullReductionService);
    }
}
