package com.lg.gulimail.order.vo;

import com.lg.gulimail.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code; // 0-成功，1-令牌失效，2-价格变动，3-库存不足
}