package com.lg.gulimail.order.vo;

import com.lg.common.vo.MemberAddressVo;
import com.lg.common.vo.OrderItemVo;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderConfirmVo {

    /** 收货地址列表 */
    @Getter
    @Setter
    private List<MemberAddressVo> address;

    /** 所有选中的购物项 */
    @Getter @Setter
    private List<OrderItemVo> items;

    /** 发票记录... */

    /** 优惠券信息（金豆） */
    @Getter @Setter
    private Integer integration;

    /** 防止重复提交的令牌 */
    @Getter @Setter
    private String orderToken;

    /** 实时库存状况（商品ID -> 是否有货） */
    @Getter @Setter
    private Map<Long, Boolean> stocks;

    // --- 以下是需要计算的属性 ---

    /** 订单总额 */
    public BigDecimal getTotal() {
        BigDecimal total = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                total = total.add(multiply);
            }
        }
        return total;
    }

    /** 应付总额 */
    public BigDecimal getPayPrice() {
        return getTotal(); // 实际逻辑中还需减去优惠金额
    }

    /** 商品件数 */
    public Integer getCount() {
        Integer i = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                i += item.getCount();
            }
        }
        return i;
    }
}