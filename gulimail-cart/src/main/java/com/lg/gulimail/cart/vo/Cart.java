package com.lg.gulimail.cart.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {
    private List<CartItem> items;
    private Integer countNum;    // 商品总数量
    private Integer countType;   // 商品类型数量
    private BigDecimal totalAmount; // 商品总价
    private BigDecimal reduce = new BigDecimal("0.00"); // 减免价格

    /**
     * 动态计算商品总数量
     */
    public Integer getCountNum() {
        int count = 0;
        if (items != null && !items.isEmpty()) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    /**
     * 动态计算商品类型数量
     */
    public Integer getCountType() {
        return items != null ? items.size() : 0;
    }

    /**
     * 动态计算总价格
     */
    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        if (items != null && !items.isEmpty()) {
            for (CartItem item : items) {
                if (item.getCheck()) {
                    amount = amount.add(item.getTotalPrice());
                }
            }
        }
        return amount.subtract(getReduce());
    }
}