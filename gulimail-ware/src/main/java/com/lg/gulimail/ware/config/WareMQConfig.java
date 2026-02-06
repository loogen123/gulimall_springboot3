package com.lg.gulimail.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WareMQConfig {

    /**
     * 定义库存扣减队列
     */
    @Bean
    public Queue stockDeductQueue() {
        // Queue(String name, boolean durable, boolean exclusive, boolean autoDelete)
        return new Queue("stock.deduct.queue", true, false, false);
    }

    /**
     * 定义绑定关系：将队列绑定到订单事件交换机
     * 注意：交换机名称必须和订单服务发送时的一致 (order-event-exchange)
     */
    @Bean
    public Binding stockDeductBinding() {
        return new Binding("stock.deduct.queue", 
                Binding.DestinationType.QUEUE,
                "order-event-exchange", 
                "order.deduct.stock", 
                null);
    }
}