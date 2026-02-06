package com.lg.gulimail.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 库存工作单
 * 
 * @author lll
 * @email lll@gmail.com
 * @date 2025-12-04 22:52:15
 */
@Data
@TableName("wms_ware_order_task_detail")
public class WareOrderTaskDetailEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * sku_id
	 */
	private Long skuId;
	/**
	 * sku_name
	 */
	private String skuName;
	/**
	 * 购买个数
	 */
	private Integer skuNum;
	/**
	 * 工作单id
	 */
	private Long taskId;

	/**
	 * 锁定状态：1-已锁定，2-已解锁，3-已扣减（扣减库存后状态）
	 */
	private Integer lockStatus;

	/**
	 * 仓库id
	 */
	private Long wareId;
}
