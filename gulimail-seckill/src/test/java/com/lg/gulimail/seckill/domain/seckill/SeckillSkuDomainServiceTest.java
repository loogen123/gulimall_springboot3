package com.lg.gulimail.seckill.domain.seckill;

import com.lg.gulimail.seckill.to.SeckillSkuRedisTo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeckillSkuDomainServiceTest {
    private final SeckillSkuDomainService seckillSkuDomainService = new SeckillSkuDomainService();

    @Test
    void validateShouldRejectWhenSkuIdInvalid() {
        SeckillSkuQueryCommand command = seckillSkuDomainService.normalize(0L);
        SeckillSkuQueryResult result = seckillSkuDomainService.validate(command);
        assertEquals(10001, result.getCode());
    }

    @Test
    void currentResultShouldNormalizeNullAsEmptyList() {
        SeckillSkuQueryResult result = seckillSkuDomainService.currentResult(null);
        assertTrue(result.isSuccess());
        assertEquals(List.of(), result.getCurrentSkus());
    }

    @Test
    void skuInfoResultShouldKeepGivenData() {
        SeckillSkuRedisTo sku = new SeckillSkuRedisTo();
        sku.setSkuId(2L);
        SeckillSkuQueryResult result = seckillSkuDomainService.skuInfoResult(sku);
        assertTrue(result.isSuccess());
        assertEquals(2L, result.getSkuInfo().getSkuId());
    }
}
