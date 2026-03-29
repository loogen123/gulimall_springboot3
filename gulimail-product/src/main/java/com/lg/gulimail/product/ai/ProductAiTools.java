package com.lg.gulimail.product.ai;

import com.alibaba.fastjson.JSON;
import com.lg.common.utils.PageUtils;
import com.lg.gulimail.product.entity.CategoryEntity;
import com.lg.gulimail.product.service.CategoryService;
import com.lg.gulimail.product.service.SkuInfoService;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductAiTools {

    private static final Logger logger = LoggerFactory.getLogger(ProductAiTools.class);

    private static final String DEFAULT_ERROR_MESSAGE = "查询失败，请稍后再试";
    private static final String NO_DATA_MESSAGE = "暂无相关数据，您可以尝试搜索其他商品或查看热门推荐";
    private static final int DEFAULT_PAGE_SIZE = 5;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private CategoryService categoryService;

    @Tool("Search products by keyword.")
    public String searchSkuInfo(String keyword) {
        logger.info("AI查询商品，关键词：{}", keyword);

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("key", keyword);
            params.put("page", "1");
            params.put("limit", String.valueOf(DEFAULT_PAGE_SIZE));

            PageUtils page = skuInfoService.queryPageByCondition(params);

            if (page != null && page.getList() != null && !page.getList().isEmpty()) {
                String result = "找到" + page.getList().size() + "个'" + keyword + "'相关商品：" + JSON.toJSONString(page.getList());
                logger.debug("商品查询成功，结果数量：{}", page.getList().size());
                return result;
            }

            logger.info("未找到关键词'{}'的商品", keyword);
            return NO_DATA_MESSAGE;

        } catch (Exception e) {
            logger.error("商品查询异常，关键词：{}", keyword, e);
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    @Tool("Get all product categories.")
    public String getAllCategories() {
        logger.info("AI获取全部分类");

        try {
            List<CategoryEntity> categories = categoryService.list();
            if (categories != null && !categories.isEmpty()) {
                logger.debug("获取到{}个分类", categories.size());
                return JSON.toJSONString(categories);
            }
            logger.info("未找到分类数据");
            return NO_DATA_MESSAGE;
        } catch (Exception e) {
            logger.error("获取分类异常", e);
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    @Tool("Get level 1 categories.")
    public String getLevel1Categories() {
        logger.info("AI获取一级分类");

        try {
            List<CategoryEntity> categories = categoryService.getLevel1Categorys();
            if (categories != null && !categories.isEmpty()) {
                logger.debug("获取到{}个一级分类", categories.size());
                return JSON.toJSONString(categories);
            }
            logger.info("未找到一级分类数据");
            return NO_DATA_MESSAGE;
        } catch (Exception e) {
            logger.error("获取一级分类异常", e);
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    @Tool("Search products by price range.")
    public String searchByPriceRange(Double minPrice, Double maxPrice, String keyword) {
        logger.info("AI按价格区间搜索商品，价格范围：{}-{}，关键词：{}", minPrice, maxPrice, keyword);

        try {
            Map<String, Object> params = new HashMap<>();
            if (keyword != null && !keyword.isEmpty()) {
                params.put("key", keyword);
            }
            params.put("page", "1");
            params.put("limit", "8");

            PageUtils page = skuInfoService.queryPageByCondition(params);

            if (page != null && page.getList() != null && !page.getList().isEmpty()) {
                logger.debug("价格区间搜索成功，结果数量：{}", page.getList().size());
                return JSON.toJSONString(page.getList());
            }

            logger.info("价格区间{}-{}未找到商品", minPrice, maxPrice);
            return NO_DATA_MESSAGE;
        } catch (Exception e) {
            logger.error("价格区间搜索异常，价格范围：{}-{}，关键词：{}", minPrice, maxPrice, keyword, e);
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    @Tool("Get hot recommendation products from the mall.")
    public String getHotRecommendations() {
        logger.info("AI获取热门推荐商品");

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("page", "1");
            params.put("limit", "6");

            PageUtils page = skuInfoService.queryPageByCondition(params);

            if (page != null && page.getList() != null && !page.getList().isEmpty()) {
                logger.debug("获取到{}个热门推荐商品", page.getList().size());
                return JSON.toJSONString(page.getList());
            }

            logger.info("未找到热门推荐商品");
            return NO_DATA_MESSAGE;
        } catch (Exception e) {
            logger.error("获取热门推荐异常", e);
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    @Tool("Get shopping help information.")
    public String getShoppingHelp() {
        logger.info("AI提供购物帮助信息");

        return "谷粒商城购物指南：\n" +
                "1. 商品搜索：在首页搜索框输入关键词\n" +
                "2. 分类浏览：点击左侧分类导航浏览商品\n" +
                "3. 加入购物车：在商品详情页点击'加入购物车'\n" +
                "4. 支付方式：支持微信、支付宝、银行卡等\n" +
                "5. 配送时间：普通商品1-3天，偏远地区3-7天\n" +
                "6. 退换政策：收到商品7天内可申请退换\n" +
                "7. 客服热线：如有问题请拨打400-888-8888";
    }
}