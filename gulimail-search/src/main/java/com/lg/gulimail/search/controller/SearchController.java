package com.lg.gulimail.search.controller;

import com.lg.gulimail.search.application.search.SearchApplicationService;
import com.lg.gulimail.search.domain.search.SearchQueryResult;
import com.lg.gulimail.search.vo.SearchParam;
import com.lg.gulimail.search.vo.SearchResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {
    @Autowired
    private SearchApplicationService searchApplicationService;

    @GetMapping({"/", "/list.html", "/search.html"})
    //根据页面提交过来的所有请求查询参数，去ES中检索商品
    public String listPage(SearchParam param, Model model, HttpServletRequest request) {
        String queryString = request.getQueryString();
        SearchQueryResult queryResult = searchApplicationService.search(param, queryString);
        SearchResult result = queryResult.getResult();
        model.addAttribute("result", result == null ? new SearchResult() : result);
        return "list";
    }
}
