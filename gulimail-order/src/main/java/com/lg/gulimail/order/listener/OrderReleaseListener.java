package com.lg.gulimail.order.listener;

import com.lg.gulimail.order.entity.OrderEntity;
import com.lg.gulimail.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderReleaseListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void handleOrderRelease(OrderEntity entity, Message message, Channel channel) throws IOException {
        // 拿到消息的交付标签（用于确认消息）
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        System.out.println("收到过期订单，准备关闭：" + entity.getOrderSn());

        try {
            // 执行关单逻辑
            orderService.closeOrder(entity);
            // 手动确认消息，false 表示不批量确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 出现异常，拒绝消息并让其重新入队（true）
            channel.basicReject(deliveryTag, true);
        }
    }
}