package com.lg.gulimail.seckill.controller;

import com.lg.common.utils.R;
import com.lg.gulimail.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/seckill/admin")
public class SeckillAdminController {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 强制重新上架（开发调试神器）
     */
    @GetMapping("/refresh")
    public R refresh() {
        // 1. 清除场次索引
        Set<String> keys = redisTemplate.keys("seckill:sessions:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        // 2. 【关键新增】清除商品详情 Hash
        // 不删这个，saveSkuInfos 里的 if(!hasKey) 就会判定为真，导致不更新数据
        redisTemplate.delete("seckill:skus");

        // 3. 【可选】清除信号量 Key，防止库存计数器冲突
        // 如果你修改了秒杀数量，这一步也是必须的
        Set<String> semaphoreKeys = redisTemplate.keys("seckill:stock:*");
        if (semaphoreKeys != null && !semaphoreKeys.isEmpty()) {
            redisTemplate.delete(semaphoreKeys);
        }

        // 4. 立即执行上架逻辑
        seckillService.uploadSeckillSkuLatest3Days();

        return R.ok("秒杀缓存已彻底刷新，标题与图片已重新同步");
    }
}