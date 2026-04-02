package com.lg.gulimail.search.domain.search;

import com.lg.gulimail.search.vo.SearchParam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchDomainServicePerformanceTest {
    @Test
    void shouldCompleteNormalizeAndValidateWithinThreshold() {
        SearchDomainService domainService = new SearchDomainService();
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            SearchParam searchParam = new SearchParam();
            searchParam.setPageNum(i % 200);
            SearchQueryCommand command = domainService.normalize(searchParam, "k=v");
            domainService.validate(command);
        }
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis <= 1200, "搜索领域规则耗时过高: " + elapsedMillis + "ms");
    }
}
