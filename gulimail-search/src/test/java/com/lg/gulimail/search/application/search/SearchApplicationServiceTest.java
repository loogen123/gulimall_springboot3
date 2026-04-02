package com.lg.gulimail.search.application.search;

import com.lg.common.to.es.SkuEsModel;
import com.lg.gulimail.search.application.port.out.ProductUpPort;
import com.lg.gulimail.search.application.port.out.SearchQueryPort;
import com.lg.gulimail.search.domain.search.ProductUpDomainService;
import com.lg.gulimail.search.domain.search.ProductUpResult;
import com.lg.gulimail.search.domain.search.SearchDomainService;
import com.lg.gulimail.search.domain.search.SearchQueryResult;
import com.lg.gulimail.search.vo.SearchParam;
import com.lg.gulimail.search.vo.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchApplicationServiceTest {
    @Mock
    private SearchQueryPort searchQueryPort;
    @Mock
    private ProductUpPort productUpPort;

    private SearchApplicationService searchApplicationService;

    @BeforeEach
    void setUp() {
        searchApplicationService =
                new SearchApplicationService(searchQueryPort, productUpPort, new SearchDomainService(), new ProductUpDomainService());
    }

    @Test
    void searchShouldCallPortAndReturnResult() {
        SearchResult searchResult = new SearchResult();
        when(searchQueryPort.search(any())).thenReturn(searchResult);
        SearchQueryResult result = searchApplicationService.search(new SearchParam(), "keyword=phone");
        assertTrue(result.isSuccess());
        assertEquals(searchResult, result.getResult());
    }

    @Test
    void productStatusUpShouldRejectEmptyList() throws Exception {
        ProductUpResult result = searchApplicationService.productStatusUp(List.of());
        assertEquals(10001, result.getCode());
        verify(productUpPort, never()).productStatusUp(any());
    }

    @Test
    void productStatusUpShouldReturnFailureWhenPortThrows() throws Exception {
        when(productUpPort.productStatusUp(any())).thenThrow(new RuntimeException("boom"));
        ProductUpResult result = searchApplicationService.productStatusUp(List.of(new SkuEsModel()));
        assertEquals(11000, result.getCode());
    }
}
