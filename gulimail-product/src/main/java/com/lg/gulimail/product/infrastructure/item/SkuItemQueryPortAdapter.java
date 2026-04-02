package com.lg.gulimail.product.infrastructure.item;

import com.lg.gulimail.product.application.port.out.SkuItemQueryPort;
import com.lg.gulimail.product.domain.item.SkuItemCommand;
import com.lg.gulimail.product.service.SkuInfoService;
import com.lg.gulimail.product.vo.SkuItemVo;
import org.springframework.stereotype.Component;

@Component
public class SkuItemQueryPortAdapter implements SkuItemQueryPort {
    private final SkuInfoService skuInfoService;

    public SkuItemQueryPortAdapter(SkuInfoService skuInfoService) {
        this.skuInfoService = skuInfoService;
    }

    @Override
    public SkuItemVo queryItem(SkuItemCommand command) {
        return skuInfoService.item(command.getSkuId());
    }
}
