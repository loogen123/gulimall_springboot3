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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;


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

        // 关键点：一定要用 "memberReceiveAddress" 这个 Key，因为它对应你 JSON 里的那个键
        MemberAddressVo data = r.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {});

        if (data != null) {
            FareVo fareVo = new FareVo();
            // 计算运费（手机尾号逻辑）
            String phone = data.getPhone();
            String fareStr = phone.substring(phone.length() - 1);
            fareVo.setFare(new BigDecimal(fareStr));
            fareVo.setAddress(data);
            return fareVo;
        }
        System.out.println("警告：地址对象解析为 null");
        return null;
    }

}