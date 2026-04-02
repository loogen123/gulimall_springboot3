package com.lg.gulimail.search.infrastructure.search;

import com.lg.common.to.es.SkuEsModel;
import com.lg.gulimail.search.application.port.out.ProductUpPort;
import com.lg.gulimail.search.service.ProductSaveService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductUpPortAdapter implements ProductUpPort {
    private final ProductSaveService productSaveService;

    public ProductUpPortAdapter(ProductSaveService productSaveService) {
        this.productSaveService = productSaveService;
    }

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws Exception {
        return productSaveService.productStatusUp(skuEsModels);
    }
}
