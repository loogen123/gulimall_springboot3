package com.lg.gulimail.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lg.gulimail.order.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-04 22:29:44
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    // 加上 @Param 注解
    int updateOrderStatus(@Param("outTradeNo") String outTradeNo, @Param("code") Integer code);
}
