package com.lg.gulimail.seckill.application.seckill;

import com.lg.gulimail.seckill.application.port.out.SeckillSkuQueryPort;
import com.lg.gulimail.seckill.domain.seckill.SeckillSkuDomainService;
import com.lg.gulimail.seckill.domain.seckill.SeckillSkuQueryResult;
import com.lg.gulimail.seckill.to.SeckillSkuRedisTo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeckillSkuApplicationServiceTest {
    @Mock
    private SeckillSkuQueryPort seckillSkuQueryPort;

    private SeckillSkuApplicationService seckillSkuApplicationService;

    @BeforeEach
    void setUp() {
        seckillSkuApplicationService = new SeckillSkuApplicationService(seckillSkuQueryPort, new SeckillSkuDomainService());
    }

    @Test
    void queryCurrentSkusShouldNormalizeNullToEmptyList() {
        when(seckillSkuQueryPort.listCurrentSkus()).thenReturn(null);
        SeckillSkuQueryResult result = seckillSkuApplicationService.queryCurrentSkus();
        assertTrue(result.isSuccess());
        assertEquals(List.of(), result.getCurrentSkus());
    }

    @Test
    void querySkuInfoShouldRejectInvalidSkuId() {
        SeckillSkuQueryResult result = seckillSkuApplicationService.querySkuInfo(0L);
        assertEquals(10001, result.getCode());
        verify(seckillSkuQueryPort, never()).getSkuInfo(anyLong());
    }

    @Test
    void querySkuInfoShouldReturnDataWhenValid() {
        SeckillSkuRedisTo sku = new SeckillSkuRedisTo();
        sku.setSkuId(3L);
        when(seckillSkuQueryPort.getSkuInfo(3L)).thenReturn(sku);
        SeckillSkuQueryResult result = seckillSkuApplicationService.querySkuInfo(3L);
        assertTrue(result.isSuccess());
        assertEquals(3L, result.getSkuInfo().getSkuId());
    }
}
