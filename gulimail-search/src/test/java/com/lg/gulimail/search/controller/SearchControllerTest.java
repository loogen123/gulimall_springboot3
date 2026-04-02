package com.lg.gulimail.search.controller;

import com.lg.gulimail.search.application.search.SearchApplicationService;
import com.lg.gulimail.search.domain.search.SearchQueryResult;
import com.lg.gulimail.search.vo.SearchParam;
import com.lg.gulimail.search.vo.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchApplicationService searchApplicationService;

    @InjectMocks
    private SearchController searchController;

    @Test
    void listPageShouldSetQueryStringAndReturnListView() {
        SearchResult searchResult = new SearchResult();
        when(searchApplicationService.search(any(SearchParam.class), eq("keyword=phone&pageNum=2")))
                .thenReturn(SearchQueryResult.success(searchResult));
        SearchParam param = new SearchParam();
        ConcurrentModel model = new ConcurrentModel();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("keyword=phone&pageNum=2");

        String view = searchController.listPage(param, model, request);

        assertEquals("list", view);
        assertSame(searchResult, model.getAttribute("result"));
    }

    @Test
    void listPageShouldClampPageNumToMax() {
        SearchResult searchResult = new SearchResult();
        when(searchApplicationService.search(any(SearchParam.class), eq(null)))
                .thenReturn(SearchQueryResult.success(searchResult));
        SearchParam param = new SearchParam();
        param.setPageNum(999);
        ConcurrentModel model = new ConcurrentModel();
        MockHttpServletRequest request = new MockHttpServletRequest();

        searchController.listPage(param, model, request);

        assertSame(searchResult, model.getAttribute("result"));
    }

    @Test
    void listPageShouldClampQueryStringLengthAndHandleNull() {
        SearchResult searchResult = new SearchResult();
        when(searchApplicationService.search(any(SearchParam.class), eq(null)))
                .thenReturn(SearchQueryResult.success(searchResult));
        SearchParam param1 = new SearchParam();
        ConcurrentModel model = new ConcurrentModel();
        MockHttpServletRequest requestWithNullQuery = new MockHttpServletRequest();

        searchController.listPage(param1, model, requestWithNullQuery);
        assertSame(searchResult, model.getAttribute("result"));

        SearchParam param2 = new SearchParam();
        MockHttpServletRequest requestWithLongQuery = new MockHttpServletRequest();
        requestWithLongQuery.setQueryString("a".repeat(2100));
        when(searchApplicationService.search(eq(param2), eq("a".repeat(2100))))
                .thenReturn(SearchQueryResult.success(searchResult));
        searchController.listPage(param2, model, requestWithLongQuery);
        assertSame(searchResult, model.getAttribute("result"));
    }
}
