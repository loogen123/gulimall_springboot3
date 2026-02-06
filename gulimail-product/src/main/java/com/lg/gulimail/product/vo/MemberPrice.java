package com.lg.gulimail.product.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberPrice { // 会员价格
    private Long id;
    private String name;
    private BigDecimal price;
}