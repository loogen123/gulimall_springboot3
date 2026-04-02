package com.lg.gulimail.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.lg.common.to.SeckillOrderTo;
import com.lg.common.utils.R;
import com.lg.common.vo.MemberResponseVo;
import com.lg.common.vo.SkuInfoVo;
import com.lg.gulimail.seckill.feign.CouponFeignService;
import com.lg.gulimail.seckill.feign.ProductFeignService;
import com.lg.gulimail.seckill.interceptor.LoginUserInterceptor;
import com.lg.gulimail.seckill.service.SeckillService;
import com.lg.gulimail.seckill.to.SeckillSkuRedisTo;
import com.lg.gulimail.seckill.vo.SeckillSessionWithSkusVo;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 13808lllgg
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    private final String SESSION_CACHE_INDEX = "seckill:sessions:index";
    private final String SKUS_CACHE_PREFIX = "seckill:skus";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";
    private final String STOCK_CACHE_INDEX = "seckill:stock:index";

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter seckillSuccessCounter;
    private Counter seckillFailCounter;

    @PostConstruct
    public void initMetrics() {
        seckillSuccessCounter = Counter.builder("gulimail.seckill.success")
                .description("秒杀成功次数")
                .register(meterRegistry);
        seckillFailCounter = Counter.builder("gulimail.seckill.fail")
                .description("秒杀失败次数")
                .register(meterRegistry);
    }

    @Override
    public void uploadSeckillSkuLatest3Days() {
        // 1. 远程调用优惠券服务，查询未来3天内的秒杀场次及关联商品
        R r = couponFeignService.getLatest3DaysSession();
        if (r.getCode() == 0) {
            // 上架商品
            List<SeckillSessionWithSkusVo> sessions = r.getData(new TypeReference<List<SeckillSessionWithSkusVo>>() {});

            // 2. 缓存到 Redis
            if (sessions != null) {
                // (1) 缓存秒杀场次信息
                saveSessionInfos(sessions);
                // (2) 缓存秒杀商品信息
                saveSkuInfos(sessions);
            }
        }
    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        long now = System.currentTimeMillis();
        Set<String> keys = redisTemplate.opsForSet().members(SESSION_CACHE_INDEX);

        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                // 解析 Key：seckill:sessions:1769824800000_1769853600000
                String replace = key.replace(SESSION_CACHE_PREFIX, "");
                String[] split = replace.split("_");
                long start = Long.parseLong(split[0]);
                long end = Long.parseLong(split[1]);

                // 2. 判断当前时间是否在场次内
                if (now >= start && now <= end) {
                    // 3. 获取该场次下所有的商品 ID (例如 ["2_1", "2_2"])
                    List<String> range = redisTemplate.opsForList().range(key, 0, -1);
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUS_CACHE_PREFIX);

                    // 4. 批量查询具体商品详情
                    List<String> list = hashOps.multiGet(range);
                    if (list != null && !list.isEmpty()) {
                        // 【关键修复】：将获取到的 JSON 字符串列表转换为对象列表并返回
                        return list.stream().map(item -> {
                            return JSON.parseObject(item, SeckillSkuRedisTo.class);
                        }).collect(Collectors.toList());
                    }
                    break;
                }
            }
        }
        return null; // 只有没匹配到场次或场次没商品才返回 null
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        // 1. 找到所有需要参与秒杀的商品 key
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUS_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();

        if (keys != null && !keys.isEmpty()) {
            // 正则匹配： 场次ID_skuId
            String regx = "\\d+_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo to = JSON.parseObject(json, SeckillSkuRedisTo.class);

                    // 2. 处理随机码：如果秒杀还没开始，不能把随机码给前端
                    long now = System.currentTimeMillis();
                    if (now < to.getStartTime() || now > to.getEndTime()) {
                        to.setRandomCode(null);
                    }
                    return to;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        // 1. 获取当前登录用户（从拦截器的 ThreadLocal 中获取）
        MemberResponseVo user = LoginUserInterceptor.loginUser.get();
        if (user == null) {
            // 如果没登录，理论上会被拦截器挡住，但这里加个兜底
            return null;
        }
        Long memberId = user.getId();

        // 2. 获取商品缓存
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUS_CACHE_PREFIX);
        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)) {
            seckillFailCounter.increment();
            return null;
        }

        SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);

        // 3. 校验合法性
        long now = System.currentTimeMillis();
        if (now < redisTo.getStartTime() || now > redisTo.getEndTime()) {
            seckillFailCounter.increment();
            return null;
        }
        if (!redisTo.getRandomCode().equals(key)) {
            seckillFailCounter.increment();
            return null;
        }
        if (num > redisTo.getSeckillLimit()) {
            seckillFailCounter.increment();
            return null;
        }

        // 4. 幂等性校验（使用真实的 memberId）
        // 防止同一个用户对同一个场次的同一个商品重复抢购
        String seckillKey = memberId + "_" + killId;
        long ttl = redisTo.getEndTime() - now;
        Boolean isFirst = redisTemplate.opsForValue().setIfAbsent(seckillKey, num.toString(), ttl, TimeUnit.MILLISECONDS);

        if (isFirst != null && isFirst) {
            // 5. 扣减信号量
            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + redisTo.getRandomCode());
            boolean acquire = semaphore.tryAcquire(num);

            if (acquire) {
                // 6. 异步下单
                try {
                    String orderSn = IdWorker.getIdStr();

                    SeckillOrderTo orderTo = new SeckillOrderTo();
                    orderTo.setOrderSn(orderSn);
                    orderTo.setMemberId(memberId); // 【核心修改】：存入真实的 memberId
                    orderTo.setNum(num);
                    orderTo.setSkuId(redisTo.getSkuId());
                    orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                    orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                    orderTo.setRandomCode(redisTo.getRandomCode());

                    log.info("准备发送秒杀消息，订单号：{}，用户ID：{}", orderSn, memberId);
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);

                    log.info("秒杀成功！订单号：{}，MQ 消息发送成功", orderSn);
                    seckillSuccessCounter.increment();
                    return orderSn;
                } catch (Exception e) {
                    log.error("MQ 发送异常：", e);
                    // 极端情况回滚：删除幂等性占位并释放信号量
                    redisTemplate.delete(seckillKey);
                    semaphore.release(num);
                    seckillFailCounter.increment();
                    return null;
                }
            } else {
                seckillFailCounter.increment();
            }
        } else {
            seckillFailCounter.increment();
        }
        return null;
    }

    private void saveSessionInfos(List<SeckillSessionWithSkusVo> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            log.warn("没有需要上架的秒杀场次");
            return;
        }

        sessions.forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;

            // 1. 幂等性检查：如果没有该场次，才进行初始化
            if (!redisTemplate.hasKey(key)) {
                // 获取该场次下所有的商品项
                List<String> collect = session.getRelationEntities().stream()
                        .map(item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString())
                        .collect(Collectors.toList());

                // 【修复关键】：必须判断 collect 是否为空，否则 leftPushAll 会抛出异常
                if (collect != null && !collect.isEmpty()) {
                    // 存入 Redis List
                    redisTemplate.opsForList().leftPushAll(key, collect);
                    redisTemplate.opsForSet().add(SESSION_CACHE_INDEX, key);

                    // 2. 设置过期时间：活动结束时间 - 当前时间 + 1天冗余
                    long ttl = endTime - System.currentTimeMillis() + 86400000L;
                    if (ttl > 0) {
                        redisTemplate.expire(key, ttl, TimeUnit.MILLISECONDS);
                    }
                    log.info("场次上架成功: {}, 包含商品数量: {}", key, collect.size());
                } else {
                    log.warn("检测到场次 {} 暂无关联商品，已跳过场次信息上架", session.getId());
                }
            }
        });
    }

    private void saveSkuInfos(List<SeckillSessionWithSkusVo> sessions) {
        sessions.forEach(session -> {
            // 绑定 Hash 操作的对象：seckill:skus
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUS_CACHE_PREFIX);

            session.getRelationEntities().forEach(seckillSkuVo -> {
                // 1. 生成缓存 Key：场次ID_商品ID
                String redisKey = seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId();

                // 2. 幂等性检查：如果 Redis 里已经上架过该商品，则不再处理
                // 注意：如果你修改了商品信息需要立即生效，请务必先去 Redis 删掉 seckill:skus 对应的 field
                if (!ops.hasKey(redisKey)) {
                    log.info("检测到新秒杀商品，准备补全信息并上架：{}", redisKey);

                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();

                    // (1) 拷贝秒杀关联表信息：价格、数量、限购等
                    BeanUtils.copyProperties(seckillSkuVo, redisTo);

                    // (2) 远程调用 Product 服务：获取商品标题和默认图片
                    try {
                        R r = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                        if (r.getCode() == 0) {
                            // 提取 Product 服务返回的 skuInfo 对象
                            SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
                            if (skuInfo != null) {
                                // 关键赋值：将 product 服务的 skuName 赋给 seckill 的 skuTitle
                                redisTo.setSkuTitle(skuInfo.getSkuName());
                                redisTo.setSkuDefaultImg(skuInfo.getSkuDefaultImg());
                            }
                        } else {
                            log.error("远程调用商品服务失败，无法补全标题图片，状态码：{}", r.getCode());
                        }
                    } catch (Exception e) {
                        log.error("远程调用商品服务异常：", e);
                    }

                    // (3) 补全场次起止时间
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());

                    // (4) 生成随机码（防刷、令牌）
                    String token = UUID.randomUUID().toString().replace("-", "");
                    redisTo.setRandomCode(token);

                    // (5) 序列化并存入 Redis Hash
                    ops.put(redisKey, JSON.toJSONString(redisTo));

                    // (6) 设置库存信号量（Redisson）
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    // 设置秒杀库存作为信号量许可
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                    redisTemplate.opsForSet().add(STOCK_CACHE_INDEX, SKU_STOCK_SEMAPHORE + token);

                    // (7) 信号量设置过期时间（建议设为场次结束时间 + 1天）
                    long ttl = session.getEndTime().getTime() - System.currentTimeMillis() + 86400000L;
                    if (ttl > 0) {
                        semaphore.expire(ttl, TimeUnit.MILLISECONDS);
                    }

                    log.info("秒杀商品详细信息上架成功：{} -> {}", redisKey, redisTo.getSkuTitle());
                }
            });
        });
    }
}
