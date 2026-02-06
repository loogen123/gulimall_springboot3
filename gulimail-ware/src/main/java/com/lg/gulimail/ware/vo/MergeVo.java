package com.lg.gulimail.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class MergeVo {
    private Long purchaseId; // 采购单id
    private List<Long> items; // [1,2,3,4] 合并的需求单id
}