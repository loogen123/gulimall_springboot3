package com.lg.gulimail.ai.service;

import com.lg.common.utils.R;
import com.lg.gulimail.ai.feign.SeckillFeignService;
import com.lg.gulimail.ai.feign.WareFeignService;
import com.lg.gulimail.ai.feign.OrderFeignService;
import com.lg.gulimail.ai.feign.ProductFeignService;
import com.alibaba.fastjson.JSON;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;

@Component
public class ProductAiTools {

    private static final Logger logger = LoggerFactory.getLogger(ProductAiTools.class);

    private static final String DEFAULT_ERROR_MESSAGE = "查询失败，请稍后再试";
    private static final String NO_DATA_MESSAGE = "暂无相关数据，您可以尝试搜索其他商品或查看热门推荐";
    private static final int DEFAULT_PAGE_SIZE = 5;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SeckillFeignService seckillFeignService;

    @Autowired
    private OrderFeignService orderFeignService;

    @Tool("Get detailed information of a specific product by its skuId.")
    public String getSkuDetails(@P("The unique ID of the SKU to get details for") Long skuId) {
        logger.info("AI查询商品详情，skuId：{}", skuId);
        try {
            R r = productFeignService.getSkuItem(skuId);
            if (r.getCode() == 0 && r.get("data") != null) {
                logger.debug("商品详情查询成功，skuId：{}", skuId);
                return JSON.toJSONString(r.get("data"));
            }
            return "未找到该商品的详细信息";
        } catch (Exception e) {
            logger.error("查询商品详情异常，skuId：{}", skuId, e);
            return DEFAULT_ERROR_MESSAGE;
        }
    }
    @Tool("Check if a specific product has stock by its skuId.")
    public String checkSkuStock(@P("The unique ID of the SKU to check stock for") Long skuId) {
        logger.info("AI查询商品库存，skuId：{}", skuId);
        try {
            R r = wareFeignService.getSkusHasStock(Arrays.asList(skuId));
            if (r.getCode() == 0) {
                logger.debug("商品库存查询成功，skuId：{}", skuId);
                return "库存查询结果：" + JSON.toJSONString(r.get("data"));
            }
            return "无法获取该商品的库存信息";
        } catch (Exception e) {
            logger.error("查询商品库存异常，skuId：{}", skuId, e);
            return "库存系统暂时不可用，请稍后再试";
        }
    }

    @Tool("Check if a specific product has active seckill (flash sale) promotion by its skuId.")
    public String checkSkuSeckill(@P("The unique ID of the SKU to check seckill promotion for") Long skuId) {
        logger.info("AI查询商品秒杀信息，skuId：{}", skuId);
        try {
            R r = seckillFeignService.getSkuSeckillInfo(skuId);
            if (r.getCode() == 0 && r.get("data") != null) {
                logger.debug("商品秒杀信息查询成功，skuId：{}", skuId);
                return "秒杀活动信息：" + JSON.toJSONString(r.get("data"));
            }
            return "该商品当前没有秒杀活动";
        } catch (Exception e) {
            logger.error("查询商品秒杀异常，skuId：{}", skuId, e);
            return "秒杀系统暂时不可用，请稍后再试";
        }
    }

    @Tool("Get the current user's order list. You MUST NOT call this tool unless the user explicitly asks about their orders.")
    public String getUserOrderList() {
        logger.info("AI查询当前用户的订单列表");
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("page", "1");
            params.put("limit", "10");
            
            String cookie = com.lg.gulimail.ai.config.FeignConfig.USER_COOKIE_THREAD_LOCAL.get();
            
            // 关键兜底：解决 LangChain4j 底层 OkHttp 异步线程池导致 InheritableThreadLocal 失效的问题！
            if (cookie == null || cookie.isEmpty()) {
                cookie = com.lg.gulimail.ai.controller.AiChatController.GLOBAL_LAST_COOKIE;
                logger.warn("【AI工具执行】ThreadLocal 中未找到 Cookie（因为线程池复用丢失了上下文），使用全局兜底 Cookie: {}", cookie);
                if (cookie != null) {
                    com.lg.gulimail.ai.config.FeignConfig.USER_COOKIE_THREAD_LOCAL.set(cookie);
                }
            } else {
                logger.info("【AI工具执行】成功从 ThreadLocal 获取到 Cookie: {}", cookie);
            }

            logger.info("当前执行工具的线程：{}", Thread.currentThread().getName());
            
            // 注意：这要求当前 AI 聊天的请求上下文中必须带有用户的登录 Token，
            // 并且需要在 Feign 配置了 RequestInterceptor 来传递这个 Token 到 order 服务。
            logger.info("准备调用 orderFeignService.listWithItem...");
            R r = orderFeignService.listWithItem(params);
            logger.info("调用 orderFeignService.listWithItem 完成，返回码：{}", r.getCode());
            
            if (r.getCode() == 0) {
                Object page = r.get("page");
                logger.debug("订单列表查询成功");
                return "这是您的最近10条订单记录：" + JSON.toJSONString(page) + 
                       "\n(订单状态: 0=待付款, 1=已付款, 2=已发货, 3=已完成, 4=已取消)";
            }
            return "无法获取订单信息，可能是您尚未登录或登录已过期。";
        } catch (Exception e) {
            logger.error("查询用户订单列表异常", e);
            return "订单系统暂时不可用或您尚未登录，请确保您已登录并在页面中重试。";
        }
    }

    @Tool("Search products by keyword.")
    public String searchSkuInfo(@P("The keyword to search for products") String keyword) {
        logger.info("AI查询商品，关键词：{}", keyword);

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("key", keyword);
            params.put("page", "1");
            params.put("limit", String.valueOf(DEFAULT_PAGE_SIZE));

            R r = productFeignService.queryPageByCondition(params);

            if (r.getCode() == 0 && r.get("page") != null) {
                Object page = r.get("page");
                String result = "找到相关商品：" + JSON.toJSONString(page);
                logger.debug("商品查询成功");
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
            R r = productFeignService.getAllCategories();
            if (r.getCode() == 0 && r.get("data") != null) {
                logger.debug("获取到全部分类");
                return JSON.toJSONString(r.get("data"));
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
            R r = productFeignService.getLevel1Categories();
            if (r.getCode() == 0 && r.get("data") != null) {
                logger.debug("获取到一级分类");
                return JSON.toJSONString(r.get("data"));
            }
            logger.info("未找到一级分类数据");
            return NO_DATA_MESSAGE;
        } catch (Exception e) {
            logger.error("获取一级分类异常", e);
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    @Tool("Get brand information by brandId.")
    public String getBrandInfo(@P("The unique ID of the brand to get info for") Long brandId) {
        logger.info("AI获取品牌信息，brandId：{}", brandId);

        try {
            R r = productFeignService.getBrandInfo(brandId);
            if (r.getCode() == 0 && r.get("brand") != null) {
                logger.debug("获取品牌信息成功，brandId：{}", brandId);
                return JSON.toJSONString(r.get("brand"));
            }
            logger.info("未找到品牌信息，brandId：{}", brandId);
            return NO_DATA_MESSAGE;
        } catch (Exception e) {
            logger.error("获取品牌信息异常，brandId：{}", brandId, e);
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    @Tool("Search products by price range.")
    public String searchByPriceRange(Double minPrice, Double maxPrice, String keyword) {
        logger.info("AI价格区间搜索，minPrice：{}, maxPrice：{}, 关键词：{}", minPrice, maxPrice, keyword);

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("min", String.valueOf(minPrice));
            params.put("max", String.valueOf(maxPrice));
            if (keyword != null && !keyword.isEmpty()) {
                params.put("key", keyword);
            }
            params.put("page", "1");
            params.put("limit", String.valueOf(DEFAULT_PAGE_SIZE));

            R r = productFeignService.queryPageByCondition(params);

            if (r.getCode() == 0 && r.get("page") != null) {
                Object page = r.get("page");
                String result = "找到相关商品：" + JSON.toJSONString(page);
                logger.debug("价格区间搜索成功");
                return result;
            }

            return "该价格区间内没有找到相关商品";
        } catch (Exception e) {
            logger.error("价格区间搜索异常", e);
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    @Tool("Search products by brand.")
    public String searchByBrand(@P("The brand name to search for") String brandName) {
        logger.info("AI按品牌搜索商品，品牌名：{}", brandName);

        try {
            // First search the brand to get brandId
            Map<String, Object> brandParams = new HashMap<>();
            brandParams.put("key", brandName);
            brandParams.put("page", "1");
            brandParams.put("limit", "1");
            // NOTE: need a feign for brand list if required. For now, search sku directly with keyword = brandName.
            Map<String, Object> params = new HashMap<>();
            params.put("key", brandName);
            params.put("page", "1");
            params.put("limit", String.valueOf(DEFAULT_PAGE_SIZE));

            R r = productFeignService.queryPageByCondition(params);

            if (r.getCode() == 0 && r.get("page") != null) {
                Object page = r.get("page");
                String result = "找到品牌相关商品：" + JSON.toJSONString(page);
                logger.debug("品牌搜索成功");
                return result;
            }

            return "未找到该品牌的商品";
        } catch (Exception e) {
            logger.error("品牌搜索异常", e);
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

            R r = productFeignService.queryPageByCondition(params);

            if (r.getCode() == 0 && r.get("page") != null) {
                Object page = r.get("page");
                logger.debug("获取到热门推荐商品");
                return JSON.toJSONString(page);
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