package com.lg.gulimail.order.web;

import com.lg.gulimail.order.application.order.SubmitOrderApplicationService;
import com.lg.gulimail.order.domain.order.OrderSubmitResult;
import com.lg.gulimail.order.service.OrderService;
import com.lg.common.utils.PageUtils;
import com.lg.gulimail.order.vo.PayAsyncVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith(MockitoExtension.class)
class OrderWebControllerTest {

    @Mock
    private OrderService orderService;
    @Mock
    private SubmitOrderApplicationService submitOrderApplicationService;

    @InjectMocks
    private OrderWebController orderWebController;

    @Test
    void submitOrderShouldRedirectConfirmWhenExceptionThrown() {
        when(submitOrderApplicationService.submitOrder(any())).thenThrow(new RuntimeException("boom"));

        String view = orderWebController.submitOrder(null, new ConcurrentModel(), new RedirectAttributesModelMap());

        assertEquals("redirect:http://order.gulimail.com/confirm.html", view);
    }

    @Test
    void submitOrderShouldRedirectPayWhenSuccess() {
        OrderSubmitResult result = new OrderSubmitResult();
        result.setCode(0);
        result.setOrderSn("order-sn-1");
        when(submitOrderApplicationService.submitOrder(any())).thenReturn(result);

        String view = orderWebController.submitOrder(null, new ConcurrentModel(), new RedirectAttributesModelMap());

        assertEquals("redirect:http://order.gulimail.com/pay.html?orderSn=order-sn-1", view);
    }

    @Test
    void handleAlipayPostShouldReturnErrorWhenRequiredFieldMissing() {
        PayAsyncVo payAsyncVo = new PayAsyncVo();
        String result = orderWebController.handleAlipayPost(payAsyncVo, new MockHttpServletRequest());
        assertEquals("error", result);
        verify(orderService, never()).handlePayResult(any());
    }

    @Test
    void handleAlipayPostShouldCallServiceWhenValid() {
        PayAsyncVo payAsyncVo = new PayAsyncVo();
        payAsyncVo.setOut_trade_no("order-1");
        payAsyncVo.setTrade_status("TRADE_SUCCESS");
        payAsyncVo.setTotal_amount("88.00");
        payAsyncVo.setTrade_no("trade-1");

        String result = orderWebController.handleAlipayPost(payAsyncVo, new MockHttpServletRequest());

        assertEquals("success", result);
        verify(orderService).handlePayResult(payAsyncVo);
    }

    @Test
    void handleAlipayPostShouldRejectUnknownTradeStatus() {
        PayAsyncVo payAsyncVo = new PayAsyncVo();
        payAsyncVo.setOut_trade_no("order-1");
        payAsyncVo.setTrade_status("WAIT_BUYER_PAY");
        payAsyncVo.setTotal_amount("88.00");
        payAsyncVo.setTrade_no("trade-1");

        String result = orderWebController.handleAlipayPost(payAsyncVo, new MockHttpServletRequest());

        assertEquals("error", result);
        verify(orderService, never()).handlePayResult(any());
    }

    @Test
    void handleAlipayPostShouldTrimRequiredFieldsBeforeCallService() {
        PayAsyncVo payAsyncVo = new PayAsyncVo();
        payAsyncVo.setOut_trade_no(" order-1 ");
        payAsyncVo.setTrade_status(" TRADE_SUCCESS ");
        payAsyncVo.setTotal_amount(" 88.00 ");
        payAsyncVo.setTrade_no(" trade-1 ");

        String result = orderWebController.handleAlipayPost(payAsyncVo, new MockHttpServletRequest());

        assertEquals("success", result);
        assertEquals("order-1", payAsyncVo.getOut_trade_no());
        assertEquals("TRADE_SUCCESS", payAsyncVo.getTrade_status());
        assertEquals("88.00", payAsyncVo.getTotal_amount());
        assertEquals("trade-1", payAsyncVo.getTrade_no());
        verify(orderService).handlePayResult(payAsyncVo);
    }

    @Test
    void handleAlipayPostShouldReturnErrorWhenTotalAmountMissing() {
        PayAsyncVo payAsyncVo = new PayAsyncVo();
        payAsyncVo.setOut_trade_no("order-1");
        payAsyncVo.setTrade_status("TRADE_SUCCESS");
        payAsyncVo.setTrade_no("trade-1");

        String result = orderWebController.handleAlipayPost(payAsyncVo, new MockHttpServletRequest());

        assertEquals("error", result);
        verify(orderService, never()).handlePayResult(any());
    }

    @Test
    void memberOrderPageShouldClampPageNumWhenTooLarge() {
        when(orderService.queryPageWithItem(argThat(params -> "100".equals(params.get("page")))))
                .thenReturn(new PageUtils(java.util.List.of(), 0, 10, 100));
        ConcurrentModel model = new ConcurrentModel();

        String view = orderWebController.memberOrderPage(999, model);

        assertEquals("list", view);
    }
}
