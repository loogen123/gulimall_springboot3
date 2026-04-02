package com.lg.gulimail.order.application.order;

import com.lg.gulimail.order.application.port.out.OrderSubmitPort;
import com.lg.gulimail.order.domain.order.OrderSubmitCommand;
import com.lg.gulimail.order.domain.order.OrderSubmitDomainService;
import com.lg.gulimail.order.domain.order.OrderSubmitResult;
import com.lg.gulimail.order.vo.SubmitOrderResponseVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitOrderApplicationServiceTest {
    @Mock
    private OrderSubmitPort orderSubmitPort;
    @Mock
    private OrderSubmitDomainService orderSubmitDomainService;
    @InjectMocks
    private SubmitOrderApplicationService submitOrderApplicationService;

    @Test
    void shouldCallDomainAndPortInOrder() {
        OrderSubmitCommand origin = new OrderSubmitCommand();
        OrderSubmitCommand normalized = new OrderSubmitCommand();
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        OrderSubmitResult result = new OrderSubmitResult();
        result.setCode(OrderSubmitResult.SUCCESS);

        when(orderSubmitDomainService.normalizeCommand(origin)).thenReturn(normalized);
        when(orderSubmitPort.submit(normalized)).thenReturn(responseVo);
        when(orderSubmitDomainService.resolveResult(responseVo)).thenReturn(result);

        OrderSubmitResult actual = submitOrderApplicationService.submitOrder(origin);

        assertEquals(OrderSubmitResult.SUCCESS, actual.normalizeCode());
        verify(orderSubmitDomainService).normalizeCommand(origin);
        verify(orderSubmitPort).submit(normalized);
        verify(orderSubmitDomainService).resolveResult(responseVo);
    }

    @Test
    void shouldReturnDomainResolvedResultWhenPortResponseNull() {
        OrderSubmitCommand command = new OrderSubmitCommand();
        OrderSubmitResult result = new OrderSubmitResult();
        result.setCode(OrderSubmitResult.TOKEN_EXPIRED);

        when(orderSubmitDomainService.normalizeCommand(any())).thenReturn(command);
        when(orderSubmitPort.submit(command)).thenReturn(null);
        when(orderSubmitDomainService.resolveResult(null)).thenReturn(result);

        OrderSubmitResult actual = submitOrderApplicationService.submitOrder(command);

        assertEquals(OrderSubmitResult.TOKEN_EXPIRED, actual.normalizeCode());
    }
}
