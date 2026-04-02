package com.lg.gulimail.product.application.item;

import com.lg.gulimail.product.application.port.out.SkuItemQueryPort;
import com.lg.gulimail.product.domain.item.SkuItemCommand;
import com.lg.gulimail.product.domain.item.SkuItemDomainService;
import com.lg.gulimail.product.domain.item.SkuItemResult;
import com.lg.gulimail.product.vo.SkuItemVo;
import org.springframework.stereotype.Service;

@Service
public class SkuItemApplicationService {
    private final SkuItemQueryPort skuItemQueryPort;
    private final SkuItemDomainService skuItemDomainService;

    public SkuItemApplicationService(SkuItemQueryPort skuItemQueryPort, SkuItemDomainService skuItemDomainService) {
        this.skuItemQueryPort = skuItemQueryPort;
        this.skuItemDomainService = skuItemDomainService;
    }

    public SkuItemResult queryItem(Long skuId) {
        SkuItemCommand command = skuItemDomainService.normalize(skuId);
        SkuItemResult validateResult = skuItemDomainService.validate(command);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }
        SkuItemVo itemVo = skuItemQueryPort.queryItem(command);
        return skuItemDomainService.success(itemVo);
    }
}
