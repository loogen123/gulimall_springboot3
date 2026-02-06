package com.lg.gulimail.order.vo;

import com.lg.gulimail.order.entity.OrderEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderQueryResultVo {
    /**
     * 订单数据列表
     */
    private List<OrderEntity> data;
    
    /**
     * 总记录数
     */
    private Integer totalCount;
    
    /**
     * 每页记录数
     */
    private Integer pageSize;
    
    /**
     * 当前页码
     */
    private Integer currPage;
    
    /**
     * 总页数
     */
    private Integer totalPage;
}