package com.lg.gulimail.product.domain.item;

import com.lg.gulimail.product.vo.SkuItemVo;
import org.springframework.stereotype.Service;

@Service
public class SkuItemDomainService {
    public SkuItemCommand normalize(Long skuId) {
        SkuItemCommand command = new SkuItemCommand();
        command.setSkuId(skuId);
        return command;
    }

    public SkuItemResult validate(SkuItemCommand command) {
        if (command == null || command.getSkuId() == null || command.getSkuId() < 1) {
            return SkuItemResult.invalidSkuId();
        }
        return SkuItemResult.success(null);
    }

    public SkuItemResult success(SkuItemVo itemVo) {
        return SkuItemResult.success(itemVo);
    }
}
