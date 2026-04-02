package com.lg.gulimail.search.domain.search;

import com.lg.gulimail.search.vo.SearchParam;
import lombok.Data;

@Data
public class SearchQueryCommand {
    private SearchParam searchParam;
}
