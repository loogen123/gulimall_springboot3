package com.lg.gulimail.search.service;

import com.lg.gulimail.search.vo.SearchParam;
import com.lg.gulimail.search.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam param);
}