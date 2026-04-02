package com.lg.gulimail.seckill.domain.seckill;

import com.lg.common.exception.BizCodeEnum;
import com.lg.gulimail.seckill.to.SeckillSkuRedisTo;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class SeckillSkuQueryResult {
    private Integer code;
    private String message;
    private List<SeckillSkuRedisTo> currentSkus;
    private SeckillSkuRedisTo skuInfo;

    public static SeckillSkuQueryResult currentSuccess(List<SeckillSkuRedisTo> currentSkus) {
        SeckillSkuQueryResult result = new SeckillSkuQueryResult();
        result.setCode(0);
        result.setMessage("success");
        result.setCurrentSkus(currentSkus == null ? Collections.emptyList() : currentSkus);
        return result;
    }

    public static SeckillSkuQueryResult skuInfoSuccess(SeckillSkuRedisTo skuInfo) {
        SeckillSkuQueryResult result = new SeckillSkuQueryResult();
        result.setCode(0);
        result.setMessage("success");
        result.setSkuInfo(skuInfo);
        return result;
    }

    public static SeckillSkuQueryResult invalidSkuId() {
        SeckillSkuQueryResult result = new SeckillSkuQueryResult();
        result.setCode(BizCodeEnum.VAILD_EXCEPTION.getCode());
        result.setMessage("skuId参数非法");
        return result;
    }

    public boolean isSuccess() {
        return code != null && code == 0;
    }
}
