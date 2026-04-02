package com.lg.gulimail.search.controller;

import com.lg.common.to.es.SkuEsModel;
import com.lg.common.utils.R;
import com.lg.gulimail.search.application.search.SearchApplicationService;
import com.lg.gulimail.search.domain.search.ProductUpResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    private SearchApplicationService searchApplicationService;

    /**
     * 上架商品：接收 Product 模块传来的 SKU 数据并存入 ES
     */
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        ProductUpResult result = searchApplicationService.productStatusUp(skuEsModels);
        if (!result.isSuccess()) {
            return R.error(result.getCode(), result.getMessage());
        }
        return R.ok();
    }
}
