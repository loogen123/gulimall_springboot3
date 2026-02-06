package com.lg.gulimail.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lg.common.to.SeckillOrderTo;
import com.lg.common.utils.PageUtils;
import com.lg.gulimail.order.entity.OrderEntity;
import com.lg.gulimail.order.vo.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-04 22:29:44
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);


    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    OrderEntity getOrderByOrderSn(String orderSn);

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    void closeOrder(OrderEntity entity);

    PayVo getOrderPay(String orderSn);

    void handlePayResult(PayAsyncVo vo);

    PageUtils queryPageWithItem(Map<String, Object> params);

    OrderEntity getOrderWithDetailsByOrderSn(String orderSn);

    void createSeckillOrder(SeckillOrderTo seckillOrder);

    OrderEntity getOrderBySn(String orderSn);
}

