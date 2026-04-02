package com.lg.gulimail.search.infrastructure.search;

import com.lg.gulimail.search.application.port.out.SearchQueryPort;
import com.lg.gulimail.search.service.MallSearchService;
import com.lg.gulimail.search.vo.SearchParam;
import com.lg.gulimail.search.vo.SearchResult;
import org.springframework.stereotype.Component;

@Component
public class SearchQueryPortAdapter implements SearchQueryPort {
    private final MallSearchService mallSearchService;

    public SearchQueryPortAdapter(MallSearchService mallSearchService) {
        this.mallSearchService = mallSearchService;
    }

    @Override
    public SearchResult search(SearchParam searchParam) {
        return mallSearchService.search(searchParam);
    }
}
