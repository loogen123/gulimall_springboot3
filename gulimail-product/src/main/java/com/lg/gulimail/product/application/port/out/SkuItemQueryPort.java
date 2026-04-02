package com.lg.gulimail.product.application.port.out;

import com.lg.gulimail.product.domain.item.SkuItemCommand;
import com.lg.gulimail.product.vo.SkuItemVo;

public interface SkuItemQueryPort {
    SkuItemVo queryItem(SkuItemCommand command);
}
