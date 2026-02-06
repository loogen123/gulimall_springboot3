package com.lg.gulimail.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySeckillMQConfig {

    /**
     * 秒杀库存回滚队列
     */
    @Bean
    public Queue seckillRollbackQueue() {
        // Queue(String name, boolean durable, boolean exclusive, boolean autoDelete)
        return new Queue("seckill.rollback.queue", true, false, false);
    }

    /**
     * 秒杀交换机（如果还没创建的话）
     */
    @Bean
    public Exchange seckillEventExchange() {
        return new TopicExchange("order-event-exchange", true, false);
    }

    /**
     * 绑定关系
     */
    @Bean
    public Binding seckillRollbackBinding() {
        return new Binding("seckill.rollback.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.rollback", // 根据你的业务定义的路由键
                null);
    }
}