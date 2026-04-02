package com.lg.gulimail.search.application.port.out;

import com.lg.common.to.es.SkuEsModel;

import java.util.List;

public interface ProductUpPort {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws Exception;
}
