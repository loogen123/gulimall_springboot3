package com.lg.gulimail.search.application.port.out;

import com.lg.gulimail.search.vo.SearchParam;
import com.lg.gulimail.search.vo.SearchResult;

public interface SearchQueryPort {
    SearchResult search(SearchParam searchParam);
}
