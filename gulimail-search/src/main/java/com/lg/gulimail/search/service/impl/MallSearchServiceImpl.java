package com.lg.gulimail.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.lg.common.to.es.SkuEsModel;
import com.lg.gulimail.search.service.MallSearchService;
import com.lg.gulimail.search.vo.SearchParam;
import com.lg.gulimail.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParam param) {
        SearchResult result = null;

        // 1. 动态构建出查询需要的 DSL 语句
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            // 2. 执行检索
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            // 3. 分析响应数据，封装成所需的格式
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            log.error("ES检索异常：{}", e.getMessage());
        }
        return result;
    }
    /**
     * 构建检索请求（DSL）
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        // 1. 构建检索源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 2. 构建查询：bool - query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 2.1 must - 模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        // 2.2 filter - 按照分类、品牌、属性、库存、价格区间过滤
        // 2.2.1 按照三级分类id查询
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        // 2.2.2 按照品牌id查询（支持多选）
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // 2.2.3 按照所有指定的属性进行查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attrStr : param.getAttrs()) {
                // attrStr = "1_5寸:6寸"
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0]; // 属性id
                String[] attrValues = s[1].split(":"); // 属性值
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                // 每一个属性过滤都必须构建一个嵌套查询（nested）
                boolQuery.filter(QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None));
            }
        }
        // 2.2.4 按照库存状态查询
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        // 2.2.5 按照价格区间查询 (skuPrice=1_500/_500/500_)

        if (StringUtils.hasText(param.getSkuPrice())) {
            // skuPrice 格式：0_500 / 500_ / _500
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_", -1); // 注意使用 -1，保留空字符串

            if (s.length == 2) {
                // 处理 500_1000 情况
                if (StringUtils.hasText(s[0])) {
                    rangeQuery.gte(s[0]);
                }
                // 处理 500_ 情况
                if (StringUtils.hasText(s[1])) {
                    rangeQuery.lte(s[1]);
                }
            } else if (s.length == 1) {
                // 处理 _500 情况
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }

        // 将构建好的 bool 查询放入 sourceBuilder
        sourceBuilder.query(boolQuery);

        // 3. 排序、分页、高亮
        // 3.1 排序 (sort=saleCount_asc/desc)
        if (!StringUtils.isEmpty(param.getSort())) {
            String[] s = param.getSort().split("_");
            sourceBuilder.sort(s[0], s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC);
        }
        // 3.2 分页 (pageSize 默认为 16)
        sourceBuilder.from((param.getPageNum() - 1) * 16);
        sourceBuilder.size(16);
        // 3.3 高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle").preTags("<b style='color:red'>").postTags("</b>");
            sourceBuilder.highlighter(builder);
        }

        // 4. 聚合分析 (侧边栏筛选)
        // 4.1 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);

        // 4.2 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);

        // 4.3 属性聚合 (nested 嵌套聚合)
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        System.out.println("构建的DSL语句："+sourceBuilder.toString());
        return new SearchRequest(new String[]{"product"}, sourceBuilder);
    }

    /**
     * 构建检索结果（解析 Response）
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();

        // 1. 返回的所有查询到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);

                // 处理高亮标题
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    if (skuTitle != null) {
                        String string = skuTitle.getFragments()[0].string();
                        esModel.setSkuTitle(string);
                    }
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);

        // 2. 当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            long attrId = bucket.getKeyAsNumber().longValue();
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                return ((Terms.Bucket) item).getKeyAsString();
            }).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        // 3. 当前所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        // 4. 当前所有商品涉及到的所有分类信息
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            catalogVo.setCatalogId(bucket.getKeyAsNumber().longValue());
            String catalogName = ((ParsedStringTerms) bucket.getAggregations().get("catalog_name_agg")).getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        // 5. 分页信息
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        // 计算总页数
        int totalPages = (int) (total + 16 - 1) / 16;
        result.setTotalPages(totalPages);
        result.setPageNum(param.getPageNum());

        // 6. 构建面包屑导航
        buildNavs(result, param);

        // ======= 新增：构建页码导航 [解决 Thymeleaf 报错的关键] =======
        List<Integer> pageNavs = new ArrayList<>();
        if (totalPages > 0) {
            // 策略：显示当前页的前2页和后2页（例如当前为5，则显示 3 4 5 6 7）
            int start = Math.max(1, param.getPageNum() - 2);
            int end = Math.min(totalPages, param.getPageNum() + 2);
            for (int i = start; i <= end; i++) {
                pageNavs.add(i);
            }
        } else {
            pageNavs.add(1); // 没数据也默认显示第1页
        }

        result.setPageNavs(pageNavs);
        // ==========================================================

        return result;
    }

    private void buildNavs(SearchResult result, SearchParam param) {
        List<SearchResult.NavVo> navs = new ArrayList<>();

        // 1. 处理【属性】面包屑
        if (param.getAttrs() != null && !param.getAttrs().isEmpty()) {
            List<Long> selectedAttrIds = new ArrayList<>();

            for (String attr : param.getAttrs()) {
                // attr 格式 = "15_以官网信息为准"
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                Long attrId = Long.parseLong(s[0]);
                selectedAttrIds.add(attrId); // 收集已选ID，用于前端隐藏筛选行

                navVo.setNavValue(s[1]);

                // 从当前的聚合结果中找属性名 (attrName)
                if (result.getAttrs() != null) {
                    result.getAttrs().stream()
                            .filter(a -> a.getAttrId().equals(attrId))
                            .findFirst()
                            .ifPresent(a -> navVo.setNavName(a.getAttrName()));
                }

                if (navVo.getNavName() == null) {
                    navVo.setNavName("属性");
                }

                // 构建取消链接
                String link = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.gulimail.com/list.html" + (link.isEmpty() ? "" : "?" + link));

                navs.add(navVo);
            }
            result.setAttrIds(selectedAttrIds);
        }

        // 2. 处理【品牌】面包屑
        if (param.getBrandId() != null && !param.getBrandId().isEmpty()) {
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");

            // 从聚合结果里找品牌名
            if (result.getBrands() != null) {
                String names = result.getBrands().stream()
                        .filter(b -> param.getBrandId().contains(b.getBrandId().toString()))
                        .map(SearchResult.BrandVo::getBrandName)
                        .collect(Collectors.joining(", "));
                navVo.setNavValue(names);
            }

            String link = replaceQueryString(param, "", "brandId");
            navVo.setLink("http://search.gulimail.com/list.html" + (link.isEmpty() ? "" : "?" + link));
            navs.add(navVo);
        }

        // 3. 处理【价格】面包屑
        if (param.getSkuPrice() != null && !param.getSkuPrice().isEmpty()) {
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("价格");
            // 格式化展示：500_999 -> 500-999元
            String price = param.getSkuPrice().replace("_", "-") + "元";
            navVo.setNavValue(price);

            String link = replaceQueryString(param, param.getSkuPrice(), "skuPrice");
            navVo.setLink("http://search.gulimail.com/list.html" + (link.isEmpty() ? "" : "?" + link));
            navs.add(navVo);
        }

        result.setNavs(navs);
    }

    /**
     * 替换/删除 URL 中的查询参数
     */
    private String replaceQueryString(SearchParam param, String value, String key) {
        String queryString = param.get_queryString();
        if (queryString == null || queryString.isEmpty()) {
            return "";
        }

        try {
            String target = "";
            if (value != null && !value.isEmpty()) {
                // 对值进行编码，匹配 URL 中的格式
                String encode = URLEncoder.encode(value, "UTF-8").replace("+", "%20");
                target = key + "=" + encode;
            } else {
                // 品牌或价格可能只需要匹配 key
                // 利用正则匹配 key=xxx 直到遇到下一个 & 或结尾
                return queryString.replaceAll("&?" + key + "=[^&]*", "").replaceAll("^&", "");
            }

            // 精准替换属性值
            String replace = queryString.replace("&" + target, "")
                    .replace(target + "&", "")
                    .replace(target, "");

            return replace.startsWith("&") ? replace.substring(1) : replace;
        } catch (UnsupportedEncodingException e) {
            return queryString;
        }
    }
}