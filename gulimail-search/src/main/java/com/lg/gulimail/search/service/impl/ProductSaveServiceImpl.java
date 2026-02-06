package com.lg.gulimail.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.lg.common.to.es.SkuEsModel;
import com.lg.gulimail.search.config.GulimailElasticSearchConfig;
import com.lg.gulimail.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 1. 创建 BulkRequest 批量请求
        BulkRequest bulkRequest = new BulkRequest();
        
        for (SkuEsModel model : skuEsModels) {
            // 2. 构造 IndexRequest (索引名必须和你在 Kibana 中建的一致，通常叫 "product")
            IndexRequest indexRequest = new IndexRequest("product");
            indexRequest.id(model.getSkuId().toString()); // 以 skuId 作为文档 id
            
            String jsonString = JSON.toJSONString(model);
            indexRequest.source(jsonString, XContentType.JSON);
            
            bulkRequest.add(indexRequest);
        }

        // 3. 执行批量操作
        // 注意：这里用的是你在配置类里定义的 COMMON_OPTIONS
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimailElasticSearchConfig.COMMON_OPTIONS);

        // 4. 处理返回结果
        boolean hasFailures = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems())
                .map(item -> item.getId())
                .collect(Collectors.toList());
        
        if (hasFailures) {
            log.error("商品上架 ES 部分保存失败：{}", bulk.buildFailureMessage());
        } else {
            log.info("商品上架 ES 成功：{}", collect);
        }

        return hasFailures;
    }
}