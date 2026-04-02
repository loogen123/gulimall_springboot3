package com.lg.gulimail.ware.listener;

import com.lg.common.to.OrderTo;
import com.lg.gulimail.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockReleaseListenerTest {

    @Mock
    private WareSkuService wareSkuService;

    @Mock
    private Channel channel;

    @InjectMocks
    private StockReleaseListener stockReleaseListener;

    @Test
    void handleOrderCloseReleaseShouldAckWhenSuccess() throws Exception {
        OrderTo orderTo = new OrderTo();
        orderTo.setOrderSn("SN1001");
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(10L);
        Message message = new Message(new byte[0], properties);

        stockReleaseListener.handleOrderCloseRelease(orderTo, message, channel);

        verify(wareSkuService).unlockStock(orderTo);
        verify(channel).basicAck(10L, false);
    }

    @Test
    void handleOrderCloseReleaseShouldRejectWhenFailed() throws Exception {
        OrderTo orderTo = new OrderTo();
        orderTo.setOrderSn("SN1002");
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(20L);
        Message message = new Message(new byte[0], properties);
        doThrow(new RuntimeException("unlock failed")).when(wareSkuService).unlockStock(orderTo);

        stockReleaseListener.handleOrderCloseRelease(orderTo, message, channel);

        verify(channel).basicReject(20L, true);
    }
}
