package com.lg.gulimail.seckill.controller;

import com.lg.common.utils.R;
import com.lg.gulimail.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/seckill/admin")
public class SeckillAdminController {
    private static final String SESSION_CACHE_INDEX = "seckill:sessions:index";
    private static final String STOCK_CACHE_INDEX = "seckill:stock:index";
    private static final int CODE_FORBIDDEN = 10003;

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${gulimail.seckill.admin.refresh-token:}")
    private String refreshToken;

    /**
     * 强制重新上架（开发调试神器）
     */
    @GetMapping("/refresh")
    public R refresh(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return R.error(CODE_FORBIDDEN, "无访问权限");
        }
        if (refreshToken != null && !refreshToken.isBlank() && !refreshToken.equals(token)) {
            return R.error(CODE_FORBIDDEN, "无访问权限");
        }
        Set<String> keys = redisTemplate.opsForSet().members(SESSION_CACHE_INDEX);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        redisTemplate.delete(SESSION_CACHE_INDEX);

        redisTemplate.delete("seckill:skus");

        Set<String> semaphoreKeys = redisTemplate.opsForSet().members(STOCK_CACHE_INDEX);
        if (semaphoreKeys != null && !semaphoreKeys.isEmpty()) {
            redisTemplate.delete(semaphoreKeys);
        }
        redisTemplate.delete(STOCK_CACHE_INDEX);

        seckillService.uploadSeckillSkuLatest3Days();

        return R.ok("秒杀缓存已彻底刷新，标题与图片已重新同步");
    }
}
