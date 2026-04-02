package com.lg.gulimail.search.domain.search;

import com.lg.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductUpDomainService {
    public ProductUpResult validate(List<SkuEsModel> skuEsModels) {
        if (skuEsModels == null || skuEsModels.isEmpty()) {
            return ProductUpResult.invalidParam();
        }
        return ProductUpResult.success();
    }
}
