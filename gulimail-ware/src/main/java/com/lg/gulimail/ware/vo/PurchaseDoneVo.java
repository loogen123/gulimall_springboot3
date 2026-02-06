package com.lg.gulimail.ware.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseDoneVo {
    @NotNull
    private Long id; // 采购单id
    private List<PurchaseItemDoneVo> items; // 每一个采购项的完成情况
}