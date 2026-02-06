package com.lg.gulimail.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.gulimail.product.dao.AttrGroupDao;
import com.lg.gulimail.product.entity.AttrAttrgroupRelationEntity;
import com.lg.gulimail.product.entity.AttrEntity;
import com.lg.gulimail.product.entity.AttrGroupEntity;
import com.lg.gulimail.product.entity.CategoryEntity;
import com.lg.gulimail.product.service.AttrAttrgroupRelationService;
import com.lg.gulimail.product.service.AttrGroupService;
import com.lg.gulimail.product.service.AttrService;
import com.lg.gulimail.product.service.CategoryService;
import com.lg.gulimail.product.vo.AttrGroupRelationVo;
import com.lg.gulimail.product.vo.AttrGroupWithAttrsVo;
import com.lg.gulimail.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private AttrAttrgroupRelationService relationService;
    @Autowired
    @Lazy
    private AttrService attrService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");

        // 1. 构造查询条件
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();

        // 2. 如果 key 不为空，拼接模糊查询条件
        if (!StringUtils.isEmpty(key)) {
            // SQL: SELECT * FROM pms_attr_group WHERE (attr_group_id = key OR attr_group_name LIKE %key%)
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }

        // 3. 判断分类逻辑
        if (catelogId == 0) {
            // 如果是 0，说明是“查询全部”，直接根据上面的 wrapper 分页
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        } else {
            // 如果不是 0，说明是在特定分类下查询
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        }
    }

    @Autowired
    private CategoryService categoryService;

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        // 递归搜集父 ID 路径
        List<Long> parentPath = findParentPath(catelogId, path);
        // 搜集出来的顺序是 [三级, 二级, 一级]，需要反转
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

// AttrGroupServiceImpl.java

    @Override
    public void addRelation(List<AttrGroupRelationVo> vos) {
        List<AttrAttrgroupRelationEntity> collect = vos.stream().map(item -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());

        // 批量保存到 pms_attr_attrgroup_relation 表
        relationService.saveBatch(collect);
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        // 1. 查询分组信息
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catelogId));

        // 2. 查询所有属性
        List<AttrGroupWithAttrsVo> collect = groupEntities.stream().map(group -> {
            AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group, vo);

            // 调用之前写好的方法：根据分组id查询关联的所有基本属性
            List<AttrEntity> attrs = attrService.getRelationAttr(vo.getAttrGroupId());
            vo.setAttrs(attrs);
            return vo;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        AttrGroupDao dao = this.baseMapper;
        return dao.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
    }

    // 辅助递归方法
    private List<Long> findParentPath(Long catelogId, List<Long> path) {
        path.add(catelogId);
        CategoryEntity byId = categoryService.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), path);
        }
        return path;
    }

}