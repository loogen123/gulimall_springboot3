package com.lg.gulimail.seckill.application.seckill;

import com.lg.gulimail.seckill.application.port.out.SeckillSkuQueryPort;
import com.lg.gulimail.seckill.domain.seckill.SeckillSkuDomainService;
import com.lg.gulimail.seckill.domain.seckill.SeckillSkuQueryCommand;
import com.lg.gulimail.seckill.domain.seckill.SeckillSkuQueryResult;
import com.lg.gulimail.seckill.to.SeckillSkuRedisTo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeckillSkuApplicationService {
    private final SeckillSkuQueryPort seckillSkuQueryPort;
    private final SeckillSkuDomainService seckillSkuDomainService;

    public SeckillSkuApplicationService(SeckillSkuQueryPort seckillSkuQueryPort,
                                        SeckillSkuDomainService seckillSkuDomainService) {
        this.seckillSkuQueryPort = seckillSkuQueryPort;
        this.seckillSkuDomainService = seckillSkuDomainService;
    }

    public SeckillSkuQueryResult queryCurrentSkus() {
        List<SeckillSkuRedisTo> currentSkus = seckillSkuQueryPort.listCurrentSkus();
        return seckillSkuDomainService.currentResult(currentSkus);
    }

    public SeckillSkuQueryResult querySkuInfo(Long skuId) {
        SeckillSkuQueryCommand command = seckillSkuDomainService.normalize(skuId);
        SeckillSkuQueryResult validateResult = seckillSkuDomainService.validate(command);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }
        SeckillSkuRedisTo skuInfo = seckillSkuQueryPort.getSkuInfo(command.getSkuId());
        return seckillSkuDomainService.skuInfoResult(skuInfo);
    }
}
