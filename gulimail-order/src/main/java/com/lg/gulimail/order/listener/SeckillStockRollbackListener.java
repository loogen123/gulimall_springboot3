package com.lg.gulimail.order.listener;

import com.lg.common.to.SeckillOrderTo;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = "seckill.rollback.queue")
public class SeckillStockRollbackListener {

    @Autowired
    private RedissonClient redissonClient;

    // 这里必须和 SeckillServiceImpl 里的前缀完全一致
    private final String SKU_STOCK_SEMAPHORE = "seckill:skus:stock:";

    @RabbitHandler
    public void handleStockRollback(SeckillOrderTo orderTo, Channel channel, Message message) throws IOException {
        log.info("收到库存回滚消息，准备释放信号量，订单号：{}", orderTo.getOrderSn());

        try {
            // 1. 获取 randomCode 拼装 Key
            String code = orderTo.getRandomCode();
            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + code);

            // 2. 归还库存
            semaphore.release(orderTo.getNum());

            // 3. 手动确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("信号量回滚成功：订单号 {}", orderTo.getOrderSn());
        } catch (Exception e) {
            log.error("回滚失败，尝试重回队列：{}", e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}