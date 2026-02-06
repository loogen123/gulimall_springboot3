package com.lg.gulimail.product.web;

import com.lg.gulimail.product.entity.CategoryEntity;
import com.lg.gulimail.product.service.CategoryService;
import com.lg.gulimail.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        // 1. 查出所有的一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();

        // 2. 放入模型中传给页面
        model.addAttribute("categorys", categoryEntities);

        // 3. 返回 Thymeleaf 逻辑视图名 (classpath:/templates/index.html)
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        return categoryService.getCatalogJson();
    }
}