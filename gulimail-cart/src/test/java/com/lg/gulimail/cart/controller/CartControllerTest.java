package com.lg.gulimail.cart.controller;

import com.lg.common.utils.R;
import com.lg.gulimail.cart.application.cart.CartSelectionApplicationService;
import com.lg.gulimail.cart.domain.cart.CartSelectionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartSelectionApplicationService cartSelectionApplicationService;

    @InjectMocks
    private CartController cartController;

    @Test
    void checkItemShouldRejectWhenCheckInvalid() {
        when(cartSelectionApplicationService.checkItem(1L, 2)).thenReturn(CartSelectionResult.invalid("check参数非法"));
        R result = cartController.checkItem(1L, 2);

        assertEquals(10001, result.getCode());
        verify(cartSelectionApplicationService).checkItem(1L, 2);
    }

    @Test
    void checkItemShouldRejectWhenSkuIdInvalid() {
        when(cartSelectionApplicationService.checkItem(0L, 1)).thenReturn(CartSelectionResult.invalid("skuId参数非法"));
        R result = cartController.checkItem(0L, 1);

        assertEquals(10001, result.getCode());
        verify(cartSelectionApplicationService).checkItem(0L, 1);
    }

    @Test
    void checkItemShouldCallServiceWhenCheckValid() {
        when(cartSelectionApplicationService.checkItem(1L, 1)).thenReturn(CartSelectionResult.success());
        R result = cartController.checkItem(1L, 1);

        assertEquals(0, result.getCode());
        verify(cartSelectionApplicationService).checkItem(1L, 1);
    }
}
