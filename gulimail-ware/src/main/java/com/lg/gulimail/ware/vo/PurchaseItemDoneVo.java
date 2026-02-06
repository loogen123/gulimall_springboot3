package com.lg.gulimail.ware.vo;

import lombok.Data;

@Data
public class PurchaseItemDoneVo {
    private Long itemId;    // 采购项id
    private Integer status; // 状态 [3-已完成，4-采购失败]
    private String reason;  // 失败原因
}