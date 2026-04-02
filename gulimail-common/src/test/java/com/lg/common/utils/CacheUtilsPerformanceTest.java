package com.lg.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheUtilsPerformanceTest {
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private CacheUtils cacheUtils;

    @BeforeEach
    void setUp() {
        cacheUtils = new CacheUtils();
        ReflectionTestUtils.setField(cacheUtils, "redisTemplate", stringRedisTemplate);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn(null);
        doNothing().when(valueOperations).set(any(), any(), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void shouldCompleteCacheAsideWithinThreshold() {
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            Integer value = cacheUtils.get("k" + i, Integer.class, () -> 1, 60, TimeUnit.SECONDS);
            assertEquals(1, value);
        }
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis <= 2000, "CacheUtils 缓存旁路耗时过高: " + elapsedMillis + "ms");
    }
}
