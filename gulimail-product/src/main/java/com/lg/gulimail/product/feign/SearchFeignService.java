package com.lg.gulimail.product.feign;

import com.lg.common.to.es.SkuEsModel;
import com.lg.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimail-search") // 必须与Nacos中检索服务的名称一致
public interface SearchFeignService {

    /**
     * 上架商品：将数据存入 Elasticsearch
     */
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}