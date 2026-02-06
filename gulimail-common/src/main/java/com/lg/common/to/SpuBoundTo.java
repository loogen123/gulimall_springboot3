package com.lg.common.to;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SpuBoundTo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long spuId;
    
    /**
     * 成长值
     */
    private BigDecimal growBounds;
    
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;
}