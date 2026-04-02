package com.lg.gulimail.order.listener;

import com.lg.gulimail.order.entity.OrderEntity;
import com.lg.gulimail.order.service.OrderService;
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
class OrderReleaseListenerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderReleaseListener orderReleaseListener;

    @Mock
    private Channel channel;

    @Test
    void handleOrderReleaseShouldAckWhenCloseSuccess() throws Exception {
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(1L);
        Message message = new Message(new byte[0], properties);
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn("o1");

        orderReleaseListener.handleOrderRelease(orderEntity, message, channel);

        verify(orderService).closeOrder(orderEntity);
        verify(channel).basicAck(1L, false);
    }

    @Test
    void handleOrderReleaseShouldRejectWhenCloseFail() throws Exception {
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(2L);
        Message message = new Message(new byte[0], properties);
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn("o2");
        doThrow(new RuntimeException("close fail")).when(orderService).closeOrder(orderEntity);

        orderReleaseListener.handleOrderRelease(orderEntity, message, channel);

        verify(channel).basicReject(2L, true);
    }
}
