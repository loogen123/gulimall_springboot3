package com.lg.common.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FareVo {
    private MemberAddressVo address; // 这里引用的是 ware 里的这个地址VO
    private BigDecimal fare;
}