package com.lg.gulimail.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRabbitConfig {
    
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 库存服务专门用来解库存的队列
    @Bean
    public Queue stockReleaseQueue() {
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    // 绑定到订单交换机上，监听 order.release.other 这个路由键
    @Bean
    public Binding stockReleaseBinding() {
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange", // 订单的交换机
                "order.release.other.#", // 监听所有以这个开头的路由键
                null);
    }
}