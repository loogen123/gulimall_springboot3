package com.lg.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SkuHasStockVo {

    private Long skuId;
    @JsonProperty("hasStock")
    private Boolean hasStock;
}