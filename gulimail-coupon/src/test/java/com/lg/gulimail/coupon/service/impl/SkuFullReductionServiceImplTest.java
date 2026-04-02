package com.lg.gulimail.coupon.service.impl;

import com.lg.common.to.SkuReductionTo;
import com.lg.gulimail.coupon.entity.MemberPriceEntity;
import com.lg.gulimail.coupon.entity.SkuFullReductionEntity;
import com.lg.gulimail.coupon.entity.SkuLadderEntity;
import com.lg.gulimail.coupon.service.MemberPriceService;
import com.lg.gulimail.coupon.service.SkuLadderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SkuFullReductionServiceImplTest {

    @Mock
    private SkuLadderService skuLadderService;

    @Mock
    private MemberPriceService memberPriceService;

    @Spy
    @InjectMocks
    private SkuFullReductionServiceImpl skuFullReductionService;

    @Test
    void saveSkuReductionShouldReturnWhenInputNull() {
        skuFullReductionService.saveSkuReduction(null);
        verifyNoInteractions(skuLadderService, memberPriceService);
    }

    @Test
    void saveSkuReductionShouldFilterInvalidMemberPrice() {
        doReturn(true).when(skuFullReductionService).save(any(SkuFullReductionEntity.class));
        SkuReductionTo reductionTo = new SkuReductionTo();
        reductionTo.setSkuId(10L);
        reductionTo.setFullPrice(new BigDecimal("100"));
        reductionTo.setReducePrice(new BigDecimal("10"));
        reductionTo.setFullCount(2);
        reductionTo.setDiscount(new BigDecimal("0.9"));
        reductionTo.setCountStatus(1);

        SkuReductionTo.MemberPrice p1 = new SkuReductionTo.MemberPrice();
        p1.setId(1L);
        p1.setName("vip");
        p1.setPrice(new BigDecimal("88"));
        SkuReductionTo.MemberPrice p2 = new SkuReductionTo.MemberPrice();
        p2.setId(2L);
        p2.setName("svip");
        p2.setPrice(BigDecimal.ZERO);
        reductionTo.setMemberPrice(List.of(p1, p2));

        skuFullReductionService.saveSkuReduction(reductionTo);

        verify(skuFullReductionService).save(any(SkuFullReductionEntity.class));
        verify(skuLadderService).save(any(SkuLadderEntity.class));
        ArgumentCaptor<List<MemberPriceEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(memberPriceService).saveBatch(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertTrue(captor.getValue().get(0).getMemberPrice().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void saveSkuReductionShouldSkipSaveWhenNoReductionData() {
        SkuReductionTo reductionTo = new SkuReductionTo();
        reductionTo.setSkuId(1L);
        reductionTo.setFullPrice(BigDecimal.ZERO);
        reductionTo.setFullCount(0);
        reductionTo.setMemberPrice(List.of());

        skuFullReductionService.saveSkuReduction(reductionTo);

        verify(skuFullReductionService, never()).save(any(SkuFullReductionEntity.class));
        verify(skuLadderService, never()).save(any(SkuLadderEntity.class));
        verify(memberPriceService, never()).saveBatch(any());
    }
}
