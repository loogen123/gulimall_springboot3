package com.lg.gulimail.order.api;

import com.lg.common.utils.R;
import com.lg.gulimail.order.application.order.SubmitOrderApplicationService;
import com.lg.gulimail.order.domain.order.OrderSubmitResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderApiControllerTest {
    @Mock
    private SubmitOrderApplicationService submitOrderApplicationService;
    @InjectMocks
    private OrderApiController orderApiController;
    private OrderSubmitRequest request;

    @BeforeEach
    void setUp() {
        request = new OrderSubmitRequest();
        request.setOrderToken("token");
    }

    @Test
    void shouldReturnOrderSnWhenSubmitSuccess() {
        OrderSubmitResult result = new OrderSubmitResult();
        result.setCode(0);
        result.setOrderSn("order-sn-1");
        when(submitOrderApplicationService.submitOrder(any())).thenReturn(result);

        R response = orderApiController.submit(request);

        assertNotNull(response);
        assertEquals(0, response.get("code"));
        assertEquals("order-sn-1", response.get("orderSn"));
    }

    @Test
    void shouldReturnErrorWhenPriceChanged() {
        OrderSubmitResult result = new OrderSubmitResult();
        result.setCode(2);
        when(submitOrderApplicationService.submitOrder(any())).thenReturn(result);

        R response = orderApiController.submit(request);

        assertEquals(2, response.get("code"));
    }
}
