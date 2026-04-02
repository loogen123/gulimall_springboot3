package com.lg.gulimail.product.application.item;

import com.lg.gulimail.product.application.port.out.SkuItemQueryPort;
import com.lg.gulimail.product.domain.item.SkuItemDomainService;
import com.lg.gulimail.product.domain.item.SkuItemResult;
import com.lg.gulimail.product.vo.SkuItemVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkuItemApplicationServiceTest {
    @Mock
    private SkuItemQueryPort skuItemQueryPort;

    private SkuItemApplicationService skuItemApplicationService;

    @BeforeEach
    void setUp() {
        skuItemApplicationService = new SkuItemApplicationService(skuItemQueryPort, new SkuItemDomainService());
    }

    @Test
    void queryItemShouldRejectWhenSkuIdInvalid() {
        SkuItemResult result = skuItemApplicationService.queryItem(0L);
        assertEquals(10001, result.getCode());
        verify(skuItemQueryPort, never()).queryItem(any());
    }

    @Test
    void queryItemShouldReturnDataWhenSkuIdValid() {
        SkuItemVo skuItemVo = new SkuItemVo();
        when(skuItemQueryPort.queryItem(any())).thenReturn(skuItemVo);
        SkuItemResult result = skuItemApplicationService.queryItem(1L);
        assertEquals(0, result.getCode());
        assertSame(skuItemVo, result.getItem());
        verify(skuItemQueryPort).queryItem(any());
    }
}
