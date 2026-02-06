package com.lg.gulimail.search.controller;

import com.lg.gulimail.search.service.MallSearchService;
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
    MallSearchService mallSearchService;

    @GetMapping({"/", "/list.html", "/search.html"})
    //根据页面提交过来的所有请求查询参数，去ES中检索商品
    public String listPage(SearchParam param, Model model, HttpServletRequest request) {
        // 必须要拿到原始的请求字符串
        String queryString = request.getQueryString();
        param.set_queryString(queryString);

        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result", result);
        return "list";
    }
}