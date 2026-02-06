package com.lg.gulimail.order.listener;

import com.lg.common.to.SeckillOrderTo;
import com.lg.gulimail.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = "order.seckill.order.queue")
public class OrderSeckillListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrder, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("收到秒杀消息，准备创建订单：{}", seckillOrder.getOrderSn());

        try {
            // 1. 核心入库逻辑
            orderService.createSeckillOrder(seckillOrder);

            // 2. 手动确认（请务必确保配置文件里配置了 spring.rabbitmq.listener.simple.acknowledge-mode=manual）
            channel.basicAck(deliveryTag, false);
            log.info("订单 {} 入库成功，消息已确认", seckillOrder.getOrderSn());

        } catch (Exception e) {
            log.error("订单入库失败或重复下单，异常信息：{}", e.getMessage());

            // 3. 这里的逻辑很关键：如果是由于【数据库唯一索引冲突】导致的异常，说明订单已经存在
            // 这种情况应该直接 Ack 掉消息，否则会死循环。
            // 如果是其他网络/数据库宕机异常，再 Reject 重回队列
            if (e.getMessage().contains("Duplicate entry") || e.getClass().getName().contains("DuplicateKeyException")) {
                channel.basicAck(deliveryTag, false);
                log.warn("检测到重复订单，已自动确认，防止死循环");
            } else {
                // 其他异常重回队列
                channel.basicReject(deliveryTag, true);
            }
        }
    }
}