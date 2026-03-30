package com.lg.gulimail.ai.feign;

import com.lg.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient("gulimail-ware") // 这里的名字必须和 Nacos 中注册的仓储服务名一致
public interface WareFeignService {

    /**
     * 查询 sku 是否有库存
     * 对应 gulimail-ware 模块中的接口
     */
    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}