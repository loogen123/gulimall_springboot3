package com.lg.gulimail.search.controller;

import com.lg.common.exception.BizCodeEnum;
import com.lg.common.to.es.SkuEsModel;
import com.lg.common.utils.R;
import com.lg.gulimail.search.application.search.SearchApplicationService;
import com.lg.gulimail.search.domain.search.ProductUpResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticSaveControllerTest {

    @Mock
    private SearchApplicationService searchApplicationService;

    @InjectMocks
    private ElasticSaveController elasticSaveController;

    @Test
    void productStatusUpShouldReturnOkWhenNoFailures() {
        when(searchApplicationService.productStatusUp(anyList())).thenReturn(ProductUpResult.success());

        R result = elasticSaveController.productStatusUp(List.of(new SkuEsModel()));

        assertEquals(0, result.getCode());
    }

    @Test
    void productStatusUpShouldReturnErrorWhenHasFailures() {
        when(searchApplicationService.productStatusUp(anyList())).thenReturn(ProductUpResult.failed());

        R result = elasticSaveController.productStatusUp(List.of(new SkuEsModel()));

        assertEquals(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), result.getCode());
    }

    @Test
    void productStatusUpShouldReturnErrorWhenExceptionThrown() {
        when(searchApplicationService.productStatusUp(anyList()))
                .thenReturn(ProductUpResult.failed());

        R result = elasticSaveController.productStatusUp(List.of(new SkuEsModel()));

        assertEquals(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), result.getCode());
    }
}
