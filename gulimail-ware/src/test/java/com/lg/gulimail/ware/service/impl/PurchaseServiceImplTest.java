package com.lg.gulimail.ware.service.impl;

import com.lg.common.constant.WareConstant;
import com.lg.gulimail.ware.entity.PurchaseDetailEntity;
import com.lg.gulimail.ware.service.PurchaseDetailService;
import com.lg.gulimail.ware.service.WareSkuService;
import com.lg.gulimail.ware.vo.PurchaseDoneVo;
import com.lg.gulimail.ware.vo.PurchaseItemDoneVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private PurchaseDetailService purchaseDetailService;

    @Mock
    private WareSkuService wareSkuService;

    @Spy
    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    @Test
    void doneShouldReturnWhenItemsEmpty() {
        PurchaseDoneVo doneVo = new PurchaseDoneVo();
        doneVo.setId(1L);
        doneVo.setItems(List.of());

        purchaseService.done(doneVo);

        verify(purchaseDetailService, never()).updateBatchById(any());
    }

    @Test
    void doneShouldUseBatchDetailsAndAddStockWhenSuccess() {
        doReturn(true).when(purchaseService).updateById(any());
        PurchaseDoneVo doneVo = new PurchaseDoneVo();
        doneVo.setId(1L);
        PurchaseItemDoneVo itemDoneVo = new PurchaseItemDoneVo();
        itemDoneVo.setItemId(11L);
        itemDoneVo.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
        doneVo.setItems(List.of(itemDoneVo));
        PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
        detailEntity.setId(11L);
        detailEntity.setSkuId(101L);
        detailEntity.setWareId(2L);
        detailEntity.setSkuNum(3);
        when(purchaseDetailService.listByIds(List.of(11L))).thenReturn(List.of(detailEntity));

        purchaseService.done(doneVo);

        verify(wareSkuService).addStock(101L, 2L, 3);
        verify(purchaseDetailService).updateBatchById(any());
        verify(purchaseService).updateById(any());
    }
}
