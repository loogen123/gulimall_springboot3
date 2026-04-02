package com.lg.gulimail.search;

import com.alibaba.fastjson.JSON;
import com.lg.gulimail.search.config.GulimailElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootTest
class GulimailSearchApplicationTests {
    @Autowired
    private RestHighLevelClient client;

    private void ensureIndexExists(String index) throws IOException {
        GetIndexRequest existsRequest = new GetIndexRequest(index);
        boolean exists = client.indices().exists(existsRequest, GulimailElasticSearchConfig.COMMON_OPTIONS);
        if (!exists) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
            client.indices().create(createIndexRequest, GulimailElasticSearchConfig.COMMON_OPTIONS);
        }
    }

    @Test
    public void searchData() throws IOException {
        ensureIndexExists("bank");
        IndexRequest seed = new IndexRequest("bank").id("seed-1");
        seed.source("{\"address\":\"mill road\",\"age\":30,\"balance\":1000}", XContentType.JSON);
        client.index(seed, GulimailElasticSearchConfig.COMMON_OPTIONS);

        // 1. 创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");

        // 2. 指定DSL检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 【修正点】使用 QueryBuilders 而不是 QueryBuilder
        sourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        //1.按年龄的值分布进行聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        sourceBuilder.aggregation(ageAgg);
        //2，计算平均薪资
        AvgAggregationBuilder blanceAvg = AggregationBuilders.avg("averageAge").field("balance");
        sourceBuilder.aggregation(blanceAvg);
        System.out.println("检索条件" + ageAgg);
        // 将条件放入请求
        searchRequest.source(sourceBuilder);

        // 3. 执行检索
        SearchResponse searchResponse = client.search(searchRequest, GulimailElasticSearchConfig.COMMON_OPTIONS);

        // 4. 打印结果（测试一下是否拿到了数据）
        System.out.println(searchResponse.toString());
    }
    /*
    * 测试存储数据到es
     */
    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        User user = new User();
        user.setUserName("张三");
        user.setAge(18);
        user.setGender("男");
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);
        // 执行操作，并接收响应对象
        IndexResponse indexResponse = client.index(indexRequest, GulimailElasticSearchConfig.COMMON_OPTIONS);
        // 提取有用的响应数据
        System.out.println("索引操作结果：" + indexResponse);
    }
    @Data
    class User{
        private String userName;
        private Integer age;
        private String gender;
    }
    @Test
    public void contextLoads() {
        System.out.println(client);
    }

    @Test
    public void searchLatencyRegression() throws IOException {
        String index = "bank_perf";
        ensureIndexExists(index);
        for (int i = 0; i < 120; i++) {
            IndexRequest seed = new IndexRequest(index).id("perf-" + i);
            int age = 20 + (i % 20);
            int balance = 1000 + i * 10;
            seed.source("{\"address\":\"mill road " + i + "\",\"age\":" + age + ",\"balance\":" + balance + "}", XContentType.JSON);
            client.index(seed, GulimailElasticSearchConfig.COMMON_OPTIONS);
        }
        List<Long> costList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            SearchRequest searchRequest = new SearchRequest(index);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
            sourceBuilder.aggregation(AggregationBuilders.terms("ageAgg").field("age").size(10));
            sourceBuilder.aggregation(AggregationBuilders.avg("averageAge").field("balance"));
            searchRequest.source(sourceBuilder);
            long start = System.nanoTime();
            SearchResponse response = client.search(searchRequest, GulimailElasticSearchConfig.COMMON_OPTIONS);
            long costMs = (System.nanoTime() - start) / 1_000_000;
            if (i > 1) {
                costList.add(costMs);
            }
            org.junit.jupiter.api.Assertions.assertTrue(response.getHits().getTotalHits().value > 0);
        }
        long p95 = percentile(costList, 0.95);
        long p50 = percentile(costList, 0.5);
        System.out.println("searchLatencyRegression p50=" + p50 + "ms, p95=" + p95 + "ms, samples=" + costList.size());
        org.junit.jupiter.api.Assertions.assertTrue(p95 <= 3000);
    }

    private long percentile(List<Long> source, double ratio) {
        List<Long> values = new ArrayList<>(source);
        Collections.sort(values);
        int index = (int) Math.ceil(values.size() * ratio) - 1;
        if (index < 0) {
            index = 0;
        }
        if (index >= values.size()) {
            index = values.size() - 1;
        }
        return values.get(index);
    }

}
