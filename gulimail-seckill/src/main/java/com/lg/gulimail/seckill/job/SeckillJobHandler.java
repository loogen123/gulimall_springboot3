package com.lg.gulimail.seckill.job;

import com.lg.gulimail.seckill.service.SeckillService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * XXL-JOB 任务处理
 */
@Slf4j
@Component
public class SeckillJobHandler {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedissonClient redissonClient;

    private final String upload_lock = "seckill:upload:lock";

    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("seckillUploadJobHandler")
    public void seckillUploadJobHandler() throws Exception {
        XxlJobHelper.log("XXL-JOB, 开始执行秒杀商品上架预热任务.");
        log.info("XXL-JOB, 开始执行秒杀商品上架预热任务.");

        // 虽然 XXL-JOB 调度中心可以控制单机执行，但为了容错，依然建议保留分布式锁
        RLock lock = redissonClient.getLock(upload_lock);
        boolean isLocked = lock.tryLock(0, 10, TimeUnit.SECONDS);
        if (isLocked) {
            try {
                seckillService.uploadSeckillSkuLatest3Days();
                XxlJobHelper.log("秒杀商品上架预热成功.");
            } finally {
                lock.unlock();
            }
        } else {
            XxlJobHelper.log("未获取到分布式锁，可能其他节点正在执行，本次跳过.");
        }
    }
}
