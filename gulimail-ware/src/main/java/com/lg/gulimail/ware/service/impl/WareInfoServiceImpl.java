package com.lg.gulimail.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lg.common.utils.PageUtils;
import com.lg.common.utils.Query;
import com.lg.common.utils.R;
import com.lg.common.vo.FareVo;
import com.lg.common.vo.MemberAddressVo;
import com.lg.gulimail.ware.dao.WareInfoDao;
import com.lg.gulimail.ware.entity.WareInfoEntity;
import com.lg.gulimail.ware.feign.MemberFeignService;
import com.lg.gulimail.ware.service.WareInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {
    @Autowired
    MemberFeignService memberFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wareInfoEntityQueryWrapper = new QueryWrapper<>();

        // 1. 获取检索关键字 key
        String key = (String) params.get("key");

        // 2. 如果 key 不为空，则进行多字段模糊匹配
        if (StringUtils.hasText(key)) {
            wareInfoEntityQueryWrapper.and(w -> {
                // SQL: WHERE (id = 'key' OR name LIKE '%key%' OR address LIKE '%key%' OR areacode LIKE '%key%')
                w.eq("id", key)
                        .or().like("name", key)
                        .or().like("address", key)
                        .or().like("areacode", key);
            });
        }

        // 3. 执行分页查询
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wareInfoEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        R r = memberFeignService.info(addrId);
        MemberAddressVo data = r.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {});

        if (data != null) {
            FareVo fareVo = new FareVo();
            String phone = data.getPhone();
            if (phone == null || phone.isBlank()) {
                fareVo.setFare(BigDecimal.ZERO);
                fareVo.setAddress(data);
                return fareVo;
            }
            String fareStr = phone.substring(phone.length() - 1);
            fareVo.setFare(new BigDecimal(fareStr));
            fareVo.setAddress(data);
            return fareVo;
        }
        log.warn("运费计算失败，地址对象为空，addrId={}", addrId);
        return null;
    }

}
