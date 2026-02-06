package com.lg.common.to;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderTo implements Serializable {
    private Long id;
    private String orderSn;
    private Integer status;
    // 只要这几个核心字段，库存服务就能根据单号去对账了
}