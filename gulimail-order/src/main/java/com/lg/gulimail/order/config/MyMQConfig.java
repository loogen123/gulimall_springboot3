package com.lg.gulimail.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyMQConfig {
    
    // 延迟队列（消息在这里存 30 分钟）
    @Bean
    public Queue orderSeckillDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "order-event-exchange"); // 死信交换机
        args.put("x-dead-letter-routing-key", "order.seckill.release"); // 死信路由键
        args.put("x-message-ttl", 1800000); // 30分钟(毫秒)
        return new Queue("order.seckill.delay.queue", true, false, false, args);
    }

    // 真正处理过期的队列
    @Bean
    public Queue orderSeckillReleaseQueue() {
        return new Queue("order.seckill.release.queue", true, false, false);
    }

    // 绑定关系
    @Bean
    public Binding orderSeckillReleaseBinding() {
        return new Binding("order.seckill.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.release",
                null);
    }
}