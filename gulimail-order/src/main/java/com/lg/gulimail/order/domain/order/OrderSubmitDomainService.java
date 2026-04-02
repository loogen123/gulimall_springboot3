package com.lg.gulimail.order.domain.order;

import com.lg.gulimail.order.vo.SubmitOrderResponseVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OrderSubmitDomainService {
    public OrderSubmitCommand normalizeCommand(OrderSubmitCommand command) {
        if (command == null) {
            return new OrderSubmitCommand();
        }
        if (StringUtils.hasText(command.getOrderToken())) {
            command.setOrderToken(command.getOrderToken().trim());
        }
        if (StringUtils.hasText(command.getRemarks())) {
            command.setRemarks(command.getRemarks().trim());
        }
        return command;
    }

    public OrderSubmitResult resolveResult(SubmitOrderResponseVo responseVo) {
        return OrderSubmitResult.from(responseVo);
    }
}
