package com.lg.gulimail.ware.controller;

import com.lg.common.utils.R;
import com.lg.gulimail.ware.entity.PurchaseEntity;
import com.lg.gulimail.ware.service.PurchaseService;
import com.lg.gulimail.ware.vo.MergeVo;
import com.lg.gulimail.ware.vo.PurchaseDoneVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PurchaseControllerTest {

    @Mock
    private PurchaseService purchaseService;

    @InjectMocks
    private PurchaseController purchaseController;

    @Test
    void receivedShouldRejectWhenIdsEmpty() {
        R result = purchaseController.received(List.of());

        assertEquals(10001, result.getCode());
        verifyNoInteractions(purchaseService);
    }

    @Test
    void finishShouldRejectWhenDoneVoIdNull() {
        PurchaseDoneVo doneVo = new PurchaseDoneVo();

        R result = purchaseController.finish(doneVo);

        assertEquals(10001, result.getCode());
        verify(purchaseService, never()).done(doneVo);
    }

    @Test
    void mergeShouldRejectWhenItemsEmpty() {
        MergeVo mergeVo = new MergeVo();
        mergeVo.setItems(List.of());

        R result = purchaseController.merge(mergeVo);

        assertEquals(10001, result.getCode());
        verify(purchaseService, never()).mergePurchase(mergeVo);
    }

    @Test
    void updateShouldRejectWhenIdNull() {
        PurchaseEntity purchaseEntity = new PurchaseEntity();

        R result = purchaseController.update(purchaseEntity);

        assertEquals(10001, result.getCode());
        verify(purchaseService, never()).updateById(purchaseEntity);
    }

    @Test
    void deleteShouldRejectWhenIdsEmpty() {
        R result = purchaseController.delete(new Long[0]);

        assertEquals(10001, result.getCode());
        verifyNoInteractions(purchaseService);
    }
}
