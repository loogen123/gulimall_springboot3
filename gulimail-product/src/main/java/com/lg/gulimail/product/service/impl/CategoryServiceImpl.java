package com.lg.gulimail.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.gulimail.product.dao.CategoryDao;
import com.lg.gulimail.product.entity.CategoryEntity;
import com.lg.gulimail.product.service.CategoryBrandRelationService;
import com.lg.gulimail.product.service.CategoryService;
import com.lg.gulimail.product.vo.Catalog2Vo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private JedisPool jedisPool;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2.组装成父子的树形结构
        //  1)找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(menu -> {
                    menu.setChildren(getChildren(menu,entities));
                    return menu;
                }).sorted((menu1, menu2)-> {
                    return (menu1.getSort()==null?0:menu1.getSort()-(menu2.getSort()==null?0:menu2.getSort()));
                })
                .collect(Collectors.toList());

        return level1Menus;
    }
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 级联更新所有关联的数据
     * @Transactional 保证分类表和关联表要么同时更新成功，要么同时回滚
     * @CacheEvict：缓存失效模式
     */
    @CacheEvict(value = "category", allEntries = true) // 级联更新时，删除 category 分区下的所有缓存
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            // 同步更新级联表中的数据（比如品牌分类关联表等）
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        // 递归搜集父节点ID
        List<Long> parentPath = findParentPath(catelogId, paths);
        // 逆序转换，因为搜集是从子到父，我们需要从父到子 [2, 34, 225]
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }
    @Cacheable(value = "category", key = "#root.methodName", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        // 只需要查出父 ID 为 0 的分类即可
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }


    // 定义缓存常量 key
    private static final String CATALOG_JSON_CACHE_KEY = "catalogJson";

    /*
    1.空结果缓存：解决缓存穿透
    2.设置过期时间（加随机值）：解决缓存雪崩
    3.加锁：解决缓存击穿
     */
    // value 对应 yml 中的 cache-name，key 是 Redis 中存储的键名
// sync = true 开启本地锁（解决单机内的缓存击穿问题）
    @Cacheable(value = "category", key = "#root.methodName", sync = true)
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        System.out.println("【SpringCache】缓存未命中，执行数据库查询逻辑...");

        // 直接调用原来的数据库查询逻辑，SpringCache 会自动把返回值存入 Redis
        return getCatalogJsonFromDb();
    }


    /**
     * 从数据库中查找并封装三级分类的数据
     */
    private Map<String, List<Catalog2Vo>> getCatalogJsonFromDb() {
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        Map<String, List<Catalog2Vo>> listMap = level1Categorys.stream().collect(Collectors.toMap(
                k -> k.getCatId().toString(),
                v -> {
                    List<CategoryEntity> level2Categorys = getParent_cid(selectList, v.getCatId());
                    List<Catalog2Vo> catalog2Vos = null;
                    if (level2Categorys != null) {
                        catalog2Vos = level2Categorys.stream().map(l2 -> {
                            Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                            List<CategoryEntity> level3Categorys = getParent_cid(selectList, l2.getCatId());
                            if (level3Categorys != null) {
                                List<Catalog2Vo.Catalog3Vo> collect = level3Categorys.stream().map(l3 ->
                                        new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName())
                                ).collect(Collectors.toList());
                                catalog2Vo.setCatalog3List(collect);
                            }
                            return catalog2Vo;
                        }).collect(Collectors.toList());
                    }
                    return catalog2Vos;
                }
        ));
        return listMap;
    }

    // 提取工具方法：在集合中筛选指定父 ID 的记录
    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        return selectList.stream().filter(item -> item.getParentCid().equals(parent_cid)).collect(Collectors.toList());
    }

    // 递归方法
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    private List<CategoryEntity> getChildren(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> children = all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .map(categoryEntity -> {
                    categoryEntity.setChildren(getChildren(categoryEntity,all));
                    return categoryEntity;
                }).sorted((menu1, menu2)->
                {
                  return (menu1.getSort()==null?0:menu1.getSort()-(menu2.getSort()==null?0:menu2.getSort()));
                }).collect(Collectors.toList());
        return children;
    }
}