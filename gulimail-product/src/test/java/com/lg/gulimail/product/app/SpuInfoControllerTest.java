package com.lg.gulimail.product.app;

import com.lg.common.utils.R;
import com.lg.gulimail.product.entity.SpuInfoEntity;
import com.lg.gulimail.product.service.SpuInfoService;
import com.lg.gulimail.product.vo.SpuSaveVo;
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
class SpuInfoControllerTest {

    @Mock
    private SpuInfoService spuInfoService;

    @InjectMocks
    private SpuInfoController spuInfoController;

    @Test
    void saveShouldRejectWhenBodyNull() {
        R result = spuInfoController.save(null);

        assertEquals(10001, result.getCode());
        verify(spuInfoService, never()).saveSpuInfo(null);
    }

    @Test
    void updateShouldRejectWhenIdNull() {
        SpuInfoEntity entity = new SpuInfoEntity();
        R result = spuInfoController.update(entity);

        assertEquals(10001, result.getCode());
        verify(spuInfoService, never()).updateById(entity);
    }

    @Test
    void deleteShouldRejectWhenIdsEmpty() {
        R result = spuInfoController.delete(new Long[0]);

        assertEquals(10001, result.getCode());
        verifyNoInteractions(spuInfoService);
    }

    @Test
    void spuUpShouldRejectWhenSpuIdInvalid() {
        R result = spuInfoController.spuUp(0L);

        assertEquals(10001, result.getCode());
        verify(spuInfoService, never()).up(0L);
    }

    @Test
    void saveShouldCallServiceWhenBodyValid() {
        SpuSaveVo vo = new SpuSaveVo();

        R result = spuInfoController.save(vo);

        assertEquals(0, result.getCode());
        verify(spuInfoService).saveSpuInfo(vo);
    }
}
