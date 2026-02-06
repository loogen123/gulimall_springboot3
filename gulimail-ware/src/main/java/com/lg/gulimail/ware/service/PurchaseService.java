package com.lg.gulimail.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lg.common.utils.PageUtils;
import com.lg.gulimail.ware.entity.PurchaseEntity;
import com.lg.gulimail.ware.vo.MergeVo;
import com.lg.gulimail.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author lll
 * @email lll@gmail.com
 * @date 2026-01-02 22:20:50
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceive(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void receivePurchase(List<Long> ids);

    void done(PurchaseDoneVo doneVo);
}

