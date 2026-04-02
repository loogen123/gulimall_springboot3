package com.lg.gulimail.product.app;

import com.lg.common.utils.R;
import com.lg.gulimail.product.application.item.SkuItemApplicationService;
import com.lg.gulimail.product.domain.item.SkuItemResult;
import com.lg.gulimail.product.entity.SkuInfoEntity;
import com.lg.gulimail.product.service.SkuInfoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkuInfoControllerTest {

    @Mock
    private SkuInfoService skuInfoService;
    @Mock
    private SkuItemApplicationService skuItemApplicationService;

    @InjectMocks
    private SkuInfoController skuInfoController;

    @Test
    void infoShouldRejectWhenSkuIdInvalid() {
        R result = skuInfoController.info(0L);

        assertEquals(10001, result.getCode());
        verify(skuInfoService, never()).getById(0L);
    }

    @Test
    void getSkuItemShouldRejectWhenSkuIdInvalid() {
        SkuItemResult invalidResult = SkuItemResult.invalidSkuId();
        when(skuItemApplicationService.queryItem(0L)).thenReturn(invalidResult);
        R result = skuInfoController.getSkuItem(0L);

        assertEquals(10001, result.getCode());
        verify(skuItemApplicationService).queryItem(0L);
    }

    @Test
    void saveShouldRejectWhenBodyNull() {
        R result = skuInfoController.save(null);

        assertEquals(10001, result.getCode());
        verify(skuInfoService, never()).save(null);
    }

    @Test
    void updateShouldRejectWhenSkuIdNull() {
        SkuInfoEntity skuInfoEntity = new SkuInfoEntity();

        R result = skuInfoController.update(skuInfoEntity);

        assertEquals(10001, result.getCode());
        verify(skuInfoService, never()).updateById(skuInfoEntity);
    }

    @Test
    void deleteShouldRejectWhenSkuIdsEmpty() {
        R result = skuInfoController.delete(new Long[0]);

        assertEquals(10001, result.getCode());
        verifyNoInteractions(skuInfoService);
    }
}
