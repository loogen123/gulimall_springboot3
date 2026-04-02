package com.lg.gulimail.search.domain.search;

import com.lg.gulimail.search.vo.SearchParam;
import com.lg.gulimail.search.vo.SearchResult;
import org.springframework.stereotype.Service;

@Service
public class SearchDomainService {
    private static final int MAX_PAGE_NUM = 100;
    private static final int MAX_QUERY_STRING_LENGTH = 2048;

    public SearchQueryCommand normalize(SearchParam param, String queryString) {
        SearchParam finalParam = param == null ? new SearchParam() : param;
        if (finalParam.getPageNum() == null || finalParam.getPageNum() < 1) {
            finalParam.setPageNum(1);
        } else if (finalParam.getPageNum() > MAX_PAGE_NUM) {
            finalParam.setPageNum(MAX_PAGE_NUM);
        }
        String finalQueryString = queryString == null ? "" : queryString;
        if (finalQueryString.length() > MAX_QUERY_STRING_LENGTH) {
            finalQueryString = finalQueryString.substring(0, MAX_QUERY_STRING_LENGTH);
        }
        finalParam.set_queryString(finalQueryString);
        SearchQueryCommand command = new SearchQueryCommand();
        command.setSearchParam(finalParam);
        return command;
    }

    public SearchQueryResult validate(SearchQueryCommand command) {
        if (command == null || command.getSearchParam() == null) {
            return SearchQueryResult.invalidParam();
        }
        return SearchQueryResult.success(null);
    }

    public SearchQueryResult success(SearchResult result) {
        return SearchQueryResult.success(result == null ? new SearchResult() : result);
    }
}
