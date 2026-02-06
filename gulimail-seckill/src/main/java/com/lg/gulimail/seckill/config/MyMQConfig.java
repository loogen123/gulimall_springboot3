package com.lg.gulimail.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyMQConfig {

    /**
     * 秒杀订单队列
     */
    @Bean
    public Queue orderSeckillOrderQueue() {
        // Queue(String name, boolean durable, boolean exclusive, boolean autoDelete)
        return new Queue("order.seckill.order.queue", true, false, false);
    }

    /**
     * 秒杀专属交换机（也可以用现有的 order-event-exchange）
     */
    @Bean
    public Exchange orderEventExchange() {
        // TopicExchange(String name, boolean durable, boolean autoDelete)
        return new TopicExchange("order-event-exchange", true, false);
    }

    /**
     * 绑定关系：将队列和交换机通过路由键绑定
     */
    @Bean
    public Binding orderSeckillOrderBinding() {
        return new Binding("order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null);
    }
}