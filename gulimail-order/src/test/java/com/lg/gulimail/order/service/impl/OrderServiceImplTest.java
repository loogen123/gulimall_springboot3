package com.lg.gulimail.order.service.impl;

import com.lg.common.to.OrderTo;
import com.lg.gulimail.order.constant.OrderStatusEnum;
import com.lg.gulimail.order.dao.OrderDao;
import com.lg.gulimail.order.entity.OrderEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void closeOrderShouldSendReleaseMessageWhenConditionalUpdateSuccess() {
        OrderServiceImpl orderService = new OrderServiceImpl();
        ReflectionTestUtils.setField(orderService, "baseMapper", orderDao);
        ReflectionTestUtils.setField(orderService, "rabbitTemplate", rabbitTemplate);

        OrderEntity entity = new OrderEntity();
        entity.setId(1L);
        entity.setOrderSn("order-sn-1");

        when(orderDao.updateOrderStatusByIdAndFromStatus(
                1L,
                OrderStatusEnum.CANCELED.getCode(),
                OrderStatusEnum.CREATE_NEW.getCode())
        ).thenReturn(1);

        orderService.closeOrder(entity);

        verify(rabbitTemplate).convertAndSend(
                eq("order-event-exchange"),
                eq("order.release.other"),
                any(OrderTo.class)
        );
    }

    @Test
    void closeOrderShouldNotSendReleaseMessageWhenConditionalUpdateFailed() {
        OrderServiceImpl orderService = new OrderServiceImpl();
        ReflectionTestUtils.setField(orderService, "baseMapper", orderDao);
        ReflectionTestUtils.setField(orderService, "rabbitTemplate", rabbitTemplate);

        OrderEntity entity = new OrderEntity();
        entity.setId(2L);
        entity.setOrderSn("order-sn-2");

        when(orderDao.updateOrderStatusByIdAndFromStatus(
                2L,
                OrderStatusEnum.CANCELED.getCode(),
                OrderStatusEnum.CREATE_NEW.getCode())
        ).thenReturn(0);

        orderService.closeOrder(entity);

        verify(rabbitTemplate, never()).convertAndSend(
                eq("order-event-exchange"),
                eq("order.release.other"),
                any(OrderTo.class)
        );
    }
}
