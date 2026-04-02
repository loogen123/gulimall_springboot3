package com.lg.gulimail.seckill.domain.seckill;

import com.lg.gulimail.seckill.to.SeckillSkuRedisTo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeckillSkuDomainService {
    public SeckillSkuQueryCommand normalize(Long skuId) {
        SeckillSkuQueryCommand command = new SeckillSkuQueryCommand();
        command.setSkuId(skuId);
        return command;
    }

    public SeckillSkuQueryResult validate(SeckillSkuQueryCommand command) {
        if (command == null || command.getSkuId() == null || command.getSkuId() < 1) {
            return SeckillSkuQueryResult.invalidSkuId();
        }
        return SeckillSkuQueryResult.skuInfoSuccess(null);
    }

    public SeckillSkuQueryResult currentResult(List<SeckillSkuRedisTo> currentSkus) {
        return SeckillSkuQueryResult.currentSuccess(currentSkus);
    }

    public SeckillSkuQueryResult skuInfoResult(SeckillSkuRedisTo skuInfo) {
        return SeckillSkuQueryResult.skuInfoSuccess(skuInfo);
    }
}
