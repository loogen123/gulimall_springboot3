package com.lg.gulimail.search.domain.search;

import com.lg.gulimail.search.vo.SearchParam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchDomainServiceTest {
    private final SearchDomainService searchDomainService = new SearchDomainService();

    @Test
    void normalizeShouldClampPageAndQueryLength() {
        SearchParam searchParam = new SearchParam();
        searchParam.setPageNum(999);
        SearchQueryCommand command = searchDomainService.normalize(searchParam, "a".repeat(2100));
        assertEquals(100, command.getSearchParam().getPageNum());
        assertEquals(2048, command.getSearchParam().get_queryString().length());
    }

    @Test
    void validateShouldRejectNullCommand() {
        SearchQueryResult result = searchDomainService.validate(null);
        assertEquals(10001, result.getCode());
    }

    @Test
    void validateShouldPassForNormalCommand() {
        SearchQueryCommand command = searchDomainService.normalize(new SearchParam(), null);
        SearchQueryResult result = searchDomainService.validate(command);
        assertTrue(result.isSuccess());
    }
}
