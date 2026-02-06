package com.lg.gulimail.ware.listener;

import com.lg.common.to.OrderTo;
import com.lg.gulimail.ware.service.WareSkuService;
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
@RabbitListener(queues = "stock.deduct.queue") // 监听扣减队列
public class StockDeductListener {

    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockDeduct(OrderTo orderTo, Message message, Channel channel) throws IOException {
        log.info("接收到扣减库存指令，单号：{}", orderTo.getOrderSn());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            // 调用 Service 执行物理扣减
            wareSkuService.orderDeductStock(orderTo.getOrderSn());

            // 成功 Ack
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("订单 {} 扣减库存异常：{}", orderTo.getOrderSn(), e.getMessage());
            // 出现异常拒绝并重新入队
            channel.basicReject(deliveryTag, true);
        }
    }
}