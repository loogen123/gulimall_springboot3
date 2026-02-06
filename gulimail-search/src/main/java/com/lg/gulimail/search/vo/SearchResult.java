package com.lg.gulimail.search.vo;

import com.lg.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResult {
    // 1. 查询到的所有商品信息
    private List<SkuEsModel> products;

    // 2. 分页信息
    private Integer pageNum;
    private Long total;
    private Integer totalPages;
    private List<Integer> pageNavs;

    // 3. 当前查询结果涉及到的所有品牌
    private List<BrandVo> brands;

    // 4. 当前查询结果涉及到的所有分类
    private List<CatalogVo> catalogs;

    // 5. 当前查询结果涉及到的所有平台属性
    private List<AttrVo> attrs;

    //6.面包屑导航
    private List<NavVo> navs = new ArrayList<>();

    //已选择的属性 ID 集合，用于前端判断哪些筛选行需要隐藏
    private List<Long> attrIds = new ArrayList<>();
    // --- 内部类 ---

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue; // 注意：一个属性名（如颜色）对应多个可选值（红色、黑色）
    }
    @Data
    public static class NavVo {
        private String navName;  // 属性名
        private String navValue; // 属性值
        private String link;     // 取消该筛选后的跳转链接
    }
}