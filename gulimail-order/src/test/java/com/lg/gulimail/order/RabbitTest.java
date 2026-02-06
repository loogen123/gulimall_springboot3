package com.lg.gulimail.order;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RabbitTest {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void sendMessage() {
        // 1. 发送内容可以是简单的字符串，也可以是具体的对象
        String msg = "Hello RabbitMQ!";
        // 2. 参数：交换机、路由键、消息内容
        // 如果没有交换机，可以发给默认交换机，routingKey 填队列名
        rabbitTemplate.convertAndSend("exchange.direct", "lg", msg);
        System.out.println("消息发送成功！");
    }
}