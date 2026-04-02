package com.lg.gulimail.order.listener;

import com.lg.gulimail.order.entity.OrderEntity;
import com.lg.gulimail.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RabbitListener(queues = "order.release.order.queue")
public class OrderReleaseListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void handleOrderRelease(OrderEntity entity, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("收到过期订单，准备关闭：{}", entity.getOrderSn());

        try {
            orderService.closeOrder(entity);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("关闭过期订单失败，orderSn={}", entity.getOrderSn(), e);
            channel.basicReject(deliveryTag, true);
        }
    }
}
