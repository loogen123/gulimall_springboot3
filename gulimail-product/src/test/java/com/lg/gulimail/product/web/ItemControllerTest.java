package com.lg.gulimail.product.web;

import com.lg.gulimail.product.application.item.SkuItemApplicationService;
import com.lg.gulimail.product.domain.item.SkuItemResult;
import com.lg.gulimail.product.vo.SkuItemVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private SkuItemApplicationService skuItemApplicationService;

    @InjectMocks
    private ItemController itemController;

    @Test
    void skuItemShouldReturnViewWithItemData() {
        SkuItemVo skuItemVo = new SkuItemVo();
        when(skuItemApplicationService.queryItem(1L)).thenReturn(SkuItemResult.success(skuItemVo));
        Model model = new ConcurrentModel();

        String view = itemController.skuItem(1L, model);

        assertEquals("item", view);
        assertSame(skuItemVo, model.getAttribute("item"));
        verify(skuItemApplicationService).queryItem(1L);
    }
}
