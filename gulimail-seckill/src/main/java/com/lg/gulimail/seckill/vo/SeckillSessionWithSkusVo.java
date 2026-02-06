package com.lg.gulimail.seckill.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class SeckillSessionWithSkusVo {
    private Long id;
    private String name;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private Date createTime;
    
    // 关键字段：场次关联的商品项
    private List<SeckillSkuRelationVo> relationEntities;
}