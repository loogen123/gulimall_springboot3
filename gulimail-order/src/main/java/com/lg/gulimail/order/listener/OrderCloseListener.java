package com.lg.gulimail.order.listener;

import com.lg.common.to.SeckillOrderTo;
import com.lg.gulimail.order.constant.OrderStatusEnum;
import com.lg.gulimail.order.entity.OrderEntity;
import com.lg.gulimail.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = "order.seckill.release.queue")
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitHandler
    public void listener(SeckillOrderTo orderTo, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("收到秒杀订单超时检查消息：{}", orderTo.getOrderSn());

        try {
            // 1. 检查数据库订单状态
            OrderEntity order = orderService.getOrderBySn(orderTo.getOrderSn());

            // 使用枚举判断：如果是“待付款”状态（通常为 0）
            if (order != null && order.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
                log.info("订单 {} 超时未支付，准备关闭订单并释放库存", orderTo.getOrderSn());

                // 2. 关闭订单（修改数据库状态为“已取消”）
                orderService.closeOrder(order);

                // 3. 发送消息给秒杀服务回滚信号量
                // 注意：这里发送的是 orderTo，确保里面带了 randomCode
                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.rollback.stock", orderTo);
            }

            // 4. 手动 Ack
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("超时检查逻辑异常，消息重回队列：{}", e.getMessage());
            // 出现网络等异常，消息重回队列等待下次重试
            channel.basicReject(deliveryTag, true);
        }
    }
}