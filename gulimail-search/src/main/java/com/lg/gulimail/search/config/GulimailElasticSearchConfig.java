package com.lg.gulimail.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GulimailElasticSearchConfig {
    public static final RequestOptions COMMON_OPTIONS;
    @Value("${gulimail.search.es.host:192.168.10.101}")
    private String esHost;

    @Value("${gulimail.search.es.port:9200}")
    private int esPort;

    @Value("${gulimail.search.es.scheme:http}")
    private String esScheme;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        // 如果有特殊请求头，可以在这里统一添加，例如：
        // builder.addHeader("Authorization", "Bearer " + TOKEN);

        COMMON_OPTIONS = builder.build();
    }
    @Bean
    public RestHighLevelClient esRestClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(esHost, esPort, esScheme)
                )
        );
        return client;
    }
}
