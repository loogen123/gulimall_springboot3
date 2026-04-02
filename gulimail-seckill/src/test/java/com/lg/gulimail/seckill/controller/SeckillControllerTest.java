package com.lg.gulimail.seckill.controller;

import com.lg.common.utils.R;
import com.lg.gulimail.seckill.application.seckill.SeckillSkuApplicationService;
import com.lg.gulimail.seckill.domain.seckill.SeckillSkuQueryResult;
import com.lg.gulimail.seckill.service.SeckillService;
import com.lg.gulimail.seckill.to.SeckillSkuRedisTo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeckillControllerTest {

    @Mock
    private SeckillService seckillService;
    @Mock
    private SeckillSkuApplicationService seckillSkuApplicationService;

    @InjectMocks
    private SeckillController seckillController;

    @Test
    void seckillShouldReturnFailWhenNumInvalid() {
        ConcurrentModel model = new ConcurrentModel();

        String view = seckillController.seckill("1_2", "k", 0, model);

        assertEquals("fail", view);
        verifyNoInteractions(seckillService);
    }

    @Test
    void seckillShouldReturnSuccessWhenOrderCreated() {
        ConcurrentModel model = new ConcurrentModel();
        when(seckillService.kill("1_2", "k", 1)).thenReturn("order-1");

        String view = seckillController.seckill("1_2", "k", 1, model);

        assertEquals("success", view);
        assertEquals("order-1", model.getAttribute("orderSn"));
    }

    @Test
    void getCurrentSeckillSkusShouldReturnEmptyListWhenNoData() {
        when(seckillSkuApplicationService.queryCurrentSkus()).thenReturn(SeckillSkuQueryResult.currentSuccess(null));

        R result = seckillController.getCurrentSeckillSkus();

        assertEquals(0, result.getCode());
        assertEquals(List.of(), result.get("data"));
    }

    @Test
    void getSkuSeckillInfoShouldRejectWhenSkuIdInvalid() {
        when(seckillSkuApplicationService.querySkuInfo(0L)).thenReturn(SeckillSkuQueryResult.invalidSkuId());

        R result = seckillController.getSkuSeckillInfo(0L);

        assertEquals(10001, result.getCode());
    }

    @Test
    void getSkuSeckillInfoShouldReturnSkuDataWhenSuccess() {
        SeckillSkuRedisTo skuRedisTo = new SeckillSkuRedisTo();
        skuRedisTo.setSkuId(1L);
        when(seckillSkuApplicationService.querySkuInfo(1L)).thenReturn(SeckillSkuQueryResult.skuInfoSuccess(skuRedisTo));

        R result = seckillController.getSkuSeckillInfo(1L);

        assertEquals(0, result.getCode());
        assertEquals(skuRedisTo, result.get("data"));
    }
}
