package com.lg.gulimail.order.application.port.out;

import com.lg.gulimail.order.domain.order.OrderSubmitCommand;
import com.lg.gulimail.order.vo.SubmitOrderResponseVo;

public interface OrderSubmitPort {
    SubmitOrderResponseVo submit(OrderSubmitCommand command);
}
