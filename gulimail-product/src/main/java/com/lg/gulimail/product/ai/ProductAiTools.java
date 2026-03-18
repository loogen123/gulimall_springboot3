package com.lg.gulimail.product.ai;

import com.alibaba.fastjson.JSON;
import com.lg.common.utils.PageUtils;
import com.lg.gulimail.product.service.SkuInfoService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProductAiTools {

    // 核心：直接注入你 Controller 里用的同一个 Service！
    @Autowired
    private SkuInfoService skuInfoService;

    @Tool("当用户询问商品、手机的价格、描述或基本信息时调用此方法。参数传商品的名称关键字。")
    public String searchSkuInfo(String keyword) {
        System.out.println("🤖 AI 正在调用 SkuInfoService 查询关键字：" + keyword);

        try {
            // 1. 模拟前端传给 Controller 的参数
            Map<String, Object> params = new HashMap<>();
            params.put("key", keyword); // 谷粒商城的模糊搜索关键字默认叫 "key"
            params.put("page", "1");    // 查第一页
            params.put("limit", "5");   // 限制最多查5条，防止数据太大撑爆大模型的 Token

            // 2. 完美复用你原有的业务逻辑！
            PageUtils page = skuInfoService.queryPageByCondition(params);

            // 3. 判断是否查到数据
            if (page != null && page.getList() != null && !page.getList().isEmpty()) {
                // 直接把查到的商品列表转成 JSON 字符串喂给大模型，它自己能看懂！
                return JSON.toJSONString(page.getList());
            }

            return "抱歉，商城中未查找到与 [" + keyword + "] 相关的商品。";

        } catch (Exception e) {
            e.printStackTrace();
            return "查询商品系统超时或报错，请稍后再试。";
        }
    }
}