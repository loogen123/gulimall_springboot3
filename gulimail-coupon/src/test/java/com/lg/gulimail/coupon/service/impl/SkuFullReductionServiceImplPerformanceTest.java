package com.lg.gulimail.coupon.service.impl;

import com.lg.common.to.SkuReductionTo;
import com.lg.gulimail.coupon.entity.SkuFullReductionEntity;
import com.lg.gulimail.coupon.entity.SkuLadderEntity;
import com.lg.gulimail.coupon.service.MemberPriceService;
import com.lg.gulimail.coupon.service.SkuLadderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class SkuFullReductionServiceImplPerformanceTest {
    @Mock
    private SkuLadderService skuLadderService;

    @Mock
    private MemberPriceService memberPriceService;

    @Spy
    @InjectMocks
    private SkuFullReductionServiceImpl skuFullReductionService;

    @Test
    void shouldCompleteReductionSaveWithinThreshold() {
        doReturn(true).when(skuFullReductionService).save(any(SkuFullReductionEntity.class));
        doReturn(true).when(skuLadderService).save(any(SkuLadderEntity.class));
        doReturn(true).when(memberPriceService).saveBatch(any());

        SkuReductionTo reductionTo = new SkuReductionTo();
        reductionTo.setSkuId(100L);
        reductionTo.setFullPrice(new BigDecimal("100"));
        reductionTo.setReducePrice(new BigDecimal("10"));
        reductionTo.setFullCount(2);
        reductionTo.setDiscount(new BigDecimal("0.90"));
        reductionTo.setCountStatus(1);
        SkuReductionTo.MemberPrice memberPrice = new SkuReductionTo.MemberPrice();
        memberPrice.setId(1L);
        memberPrice.setName("vip");
        memberPrice.setPrice(new BigDecimal("88"));
        reductionTo.setMemberPrice(List.of(memberPrice));

        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            skuFullReductionService.saveSkuReduction(reductionTo);
        }
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis <= 3000, "满减写入规则耗时过高: " + elapsedMillis + "ms");
    }
}
