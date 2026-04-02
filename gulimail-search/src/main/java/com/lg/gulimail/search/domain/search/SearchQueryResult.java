package com.lg.gulimail.search.domain.search;

import com.lg.common.exception.BizCodeEnum;
import com.lg.gulimail.search.vo.SearchResult;
import lombok.Data;

@Data
public class SearchQueryResult {
    private Integer code;
    private String message;
    private SearchResult result;

    public static SearchQueryResult success(SearchResult result) {
        SearchQueryResult queryResult = new SearchQueryResult();
        queryResult.setCode(0);
        queryResult.setMessage("success");
        queryResult.setResult(result);
        return queryResult;
    }

    public static SearchQueryResult invalidParam() {
        SearchQueryResult queryResult = new SearchQueryResult();
        queryResult.setCode(BizCodeEnum.VAILD_EXCEPTION.getCode());
        queryResult.setMessage("检索参数不合法");
        return queryResult;
    }

    public boolean isSuccess() {
        return code != null && code == 0;
    }
}
