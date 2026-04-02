package com.lg.gulimail.cart.application.cart;

import com.lg.gulimail.cart.application.port.out.CartSelectionPort;
import com.lg.gulimail.cart.domain.cart.CartSelectionDomainService;
import com.lg.gulimail.cart.domain.cart.CartSelectionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartSelectionApplicationServiceTest {
    @Mock
    private CartSelectionPort cartSelectionPort;
    @Mock
    private CartSelectionDomainService cartSelectionDomainService;
    @InjectMocks
    private CartSelectionApplicationService applicationService;

    @Test
    void checkItemShouldNotCallPortWhenValidateFailed() {
        CartSelectionResult invalid = CartSelectionResult.invalid("skuId参数非法");
        org.mockito.Mockito.when(cartSelectionDomainService.normalize(org.mockito.ArgumentMatchers.any())).thenCallRealMethod();
        org.mockito.Mockito.when(cartSelectionDomainService.validate(org.mockito.ArgumentMatchers.any())).thenReturn(invalid);

        CartSelectionResult result = applicationService.checkItem(0L, 1);

        assertEquals(10001, result.getCode());
        verify(cartSelectionPort, never()).checkItem(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void checkItemShouldCallPortWhenValidatePassed() {
        org.mockito.Mockito.when(cartSelectionDomainService.normalize(org.mockito.ArgumentMatchers.any())).thenCallRealMethod();
        org.mockito.Mockito.when(cartSelectionDomainService.validate(org.mockito.ArgumentMatchers.any())).thenReturn(CartSelectionResult.success());

        CartSelectionResult result = applicationService.checkItem(1L, 1);

        assertEquals(0, result.getCode());
        verify(cartSelectionPort).checkItem(1L, 1);
    }
}
