package com.lg.gulimail.search.service;

import com.lg.common.to.es.SkuEsModel;
import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
    /**
     * 将商品信息保存到 ES
     * @param skuEsModels 组装好的 ES 数据
     * @return 是否有错误 (true 表示有错误，false 表示全部成功)
     */
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}